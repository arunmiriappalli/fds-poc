
# FDS POC (Local, synchronous decisioning)

A minimal, **fully local** fraud decisioning gateway with dynamic rules and Redis-backed sliding windows.

The fraud detection system that monitors card transactions real-time, on predefined rules. Every transaction is screened and allowed only if it passes the criterion.

Example rules
- If location is beyond 1KM from the registered location, block the transaction
- If total number of transaction is more than 10 in the last 5 min window, block the device used
- If the same card is used more than 5 on the same device in the last 1 hour, block the device

Transaction information
- Card used
- Transaction Amount
- Location coordinates
- Device used to swipe the card

# How to build and start the system

1. wait for containers to get healthy (first time pull will take a bit)
> docker compose up --build -d

Incase of change in code
> docker compose build --no-cache app
> docker compose up -d

To check app logs
> docker compose logs -f app


2. Load the 3 example rules
> ./scripts/add_rules.sh

3. Fire demo traffic to see the blocks in action (needs `jq`)
> ./scripts/demo_txns.sh

# Try it manually

## List rules
> curl -s http://localhost:8080/rules | jq

## Authorize a transaction
> curl -s http://localhost:8080/transactions/authorize -H 'Content-Type: application/json' -d '{
  "txn_id":"t1",
  "ts":"2025-08-11T10:00:00Z",
  "card_id":"c123",
  "amount":1499,
  "lat":12.935,
  "lon":77.611,
  "device_id":"d42"
}'

# Goals (POC)
- **Sync, inline decisioning**: each /authorize call gets ALLOW/BLOCK immediately.
- **Dynamic rules**: edit rules without redeploys.
- **Low ceremony**: all services run via docker compose.

# Architecture
Synchronous gateway with in-memory rule engine + Redis windows

```
Client → Transaction Gateway (Spring Boot)
         ├─ loads active rules from Postgres (cached, hot-reload)
         ├─ evaluates stateless rules (distance, card flags, etc)
         ├─ evaluates stateful rules via Redis time windows (atomic Lua)
         ├─ sets/reads blocklists in Redis (with TTL)
         └─ returns ALLOW/BLOCK (+ reason code)

Postgres → metadata (rules, cards/devices, registered locations)
Redis    → sliding-window counters, dedupe keys, blocklists
```

# Tech stack
- **Language**: Java 21, Spring Boot 3 (Web, Data JDBC, Actuator)
- **Store**: PostgreSQL 16 (metadata), plain tables (no PostGIS needed for POC)
- **Cache/State**: Redis 7 (Sorted Sets + Lua for sliding windows; Strings for blocklists)
- **Rule engine**: CEL (cel-java) for safe, dynamic boolean expressions
- **Build/Run**: Maven + Docker Compose

# Rule model (simple, powerful)
Use types to keep evaluation clear and fast. Back them with CEL helpers so rules are editable text.

1. Geo-distance rule
```
{
  "rule_id":"geo-1km",
  "type":"geo_distance",
  "expr":"distance_km(txn.lat, txn.lon, card.reg_lat, card.reg_lon) > params.max_km",
  "params": {"max_km": 1},
  "action": {"decision":"BLOCK", "entity":"TRANSACTION", "reason":"GEO_DISTANCE_EXCEEDED"},
  "priority": 10, "enabled": true
}
```
2. Frequency by device (5 min)

```{
  "rule_id":"dev-10-in-5m",
  "type":"count_threshold",
  "expr":"count_in_window(key('device', txn.device_id), 'PT5M') > params.threshold",
  "params": {"threshold": 10},
  "action": {"decision":"BLOCK", "entity":"DEVICE", "ttl":"PT1H",
             "reason":"DEVICE_TXN_RATE_EXCEEDED"},
  "priority": 20, "enabled": true
}
```

3. Same card on same device (1 hour)

```
{
  "rule_id":"card-device-5-in-1h",
  "type":"count_threshold",
  "expr":"count_in_window(key('cardDevice', txn.card_id, txn.device_id), 'PT1H') > params.threshold",
  "params": {"threshold": 5},
  "action": {"decision":"BLOCK", "entity":"DEVICE", "ttl":"PT4H",
             "reason":"CARD_DEVICE_ABUSE"},
  "priority": 30, "enabled": true
}
```

# How the checks work (tight & fast)
- Geo: Haversine in Java utility (distance_km) using doubles.
- Blocklists: before eval, check redis GET block:device:{device_id} / block:card:{card_id}.
- Windowed counts (Redis Sorted Sets + Lua, atomic):
  - Key examples:
    - win:device:{device_id} (score = epoch ms, member = txn_id)
    - win:cardDevice:{card_id}:{device_id}
  - Lua steps per call: ZADD now → ZREMRANGEBYSCORE older-than-window → ZCARD → return count.
  - That’s O(logN) and single round-trip.

# Request flow (sync decision)
1. POST /transactions/authorize with txn JSON (+ optional Idempotency-Key).

2. Gateway:
   1. Reject if device/card blocklist present.
   2. Pull card’s registered lat/lon once (Postgres; cache by Caffeine/Redis).
   3. For each enabled rule by priority:
      1. Evaluate expr with helpers (CEL).
      2. For count_in_window(..), call the Redis Lua and get the current count.
      3. On first true (violation), apply action:
         1. Set block:device:{id} with TTL if action targets device.
         2. Return BLOCK with reason.
         3. 
3. If no rule violates → ALLOW.

4. Write a decision log to Postgres (txn_decisions) for audit (txn_id, decision, reason, matched_rule_id, timestamp, evaluated_counts…).
