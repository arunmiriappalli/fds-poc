
#!/usr/bin/env bash
set -euo pipefail
base="${1:-http://localhost:8080}"

echo "=== GEO test: expect BLOCK (distance > 1km) ==="
curl -s -X POST "$base/transactions/authorize" -H 'Content-Type: application/json' -d '{
  "txn_id":"t-geo-1",
  "ts":"2025-08-11T10:00:00Z",
  "card_id":"c123",
  "amount":100,
  "lat":12.95,
  "lon":77.63,
  "device_id":"d42"
}' | jq .
echo

echo "=== Rate test: 11 txns on device d42 within 5m, expect BLOCK and device blocklist ==="
for i in $(seq 1 11); do
  curl -s -X POST "$base/transactions/authorize" -H 'Content-Type: application/json' -d "{
    \"txn_id\": \"t-burst-$i\",
    \"ts\": \"2025-08-11T10:00:00Z\",
    \"card_id\": \"c123\",
    \"amount\": 50,
    \"lat\": 12.935,
    \"lon\": 77.611,
    \"device_id\": \"d42\"
  }" | jq -r '.decision + ": " + .reason'
done
echo

echo "=== Card-Device test: 6 txns same card/device in 1h, expect BLOCK ==="
for i in $(seq 1 6); do
  curl -s -X POST "$base/transactions/authorize" -H 'Content-Type: application/json' -d "{
    \"txn_id\": \"t-cd-$i\",
    \"ts\": \"2025-08-11T10:00:00Z\",
    \"card_id\": \"c123\",
    \"amount\": 75,
    \"lat\": 12.935,
    \"lon\": 77.611,
    \"device_id\": \"d42\"
  }" | jq -r '.decision + ": " + .reason'
done
echo
