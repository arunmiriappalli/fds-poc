CREATE TABLE IF NOT EXISTS cards (
  card_id TEXT PRIMARY KEY,
  reg_lat DOUBLE PRECISION NOT NULL,
  reg_lon DOUBLE PRECISION NOT NULL
);

CREATE TABLE IF NOT EXISTS devices (
  device_id TEXT PRIMARY KEY
);

CREATE TABLE IF NOT EXISTS rules (
  id BIGSERIAL PRIMARY KEY,
  type TEXT NOT NULL,              -- GEO_DISTANCE | COUNT_THRESHOLD
  key_kind TEXT,                   -- device | cardDevice (for COUNT_THRESHOLD)
  time_window TEXT,                -- ISO-8601 duration e.g. PT5M
  threshold INTEGER,
  max_km DOUBLE PRECISION,         -- for GEO_DISTANCE
  action_decision TEXT NOT NULL,   -- BLOCK | ALLOW
  action_entity TEXT NOT NULL,     -- TRANSACTION | DEVICE | CARD
  action_ttl_seconds INTEGER,      -- optional
  reason TEXT NOT NULL,
  priority INTEGER NOT NULL DEFAULT 100,
  enabled BOOLEAN NOT NULL DEFAULT TRUE,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS txn_decisions (
  txn_id TEXT PRIMARY KEY,
  ts TIMESTAMPTZ NOT NULL DEFAULT now(),
  decision TEXT NOT NULL,
  reason TEXT,
  matched_rule_id BIGINT,
  details JSONB
);

INSERT INTO cards(card_id, reg_lat, reg_lon) VALUES
  ('c123', 12.935, 77.611)
ON CONFLICT (card_id) DO NOTHING;

INSERT INTO devices(device_id) VALUES ('d42')
ON CONFLICT (device_id) DO NOTHING;