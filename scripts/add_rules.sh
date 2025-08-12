
#!/usr/bin/env bash
set -euo pipefail
base="${1:-http://localhost:8080}"

echo "Adding GEO_DISTANCE >1km rule (BLOCK transaction)"
curl -s -X POST "$base/rules" -H 'Content-Type: application/json' -d '{
  "type":"GEO_DISTANCE",
  "maxKm":1.0,
  "actionDecision":"BLOCK",
  "actionEntity":"TRANSACTION",
  "reason":"GEO_DISTANCE_EXCEEDED",
  "priority":10,
  "enabled":true
}' && echo

echo "Adding device >10 txns in 5m rule (BLOCK device 1h)"
curl -s -X POST "$base/rules" -H 'Content-Type: application/json' -d '{
  "type":"COUNT_THRESHOLD",
  "keyKind":"device",
  "window":"PT5M",
  "threshold":10,
  "actionDecision":"BLOCK",
  "actionEntity":"DEVICE",
  "actionTtlSeconds":3600,
  "reason":"DEVICE_TXN_RATE_EXCEEDED",
  "priority":20,
  "enabled":true
}' && echo

echo "Adding same card on same device >5 in 1h rule (BLOCK device 4h)"
curl -s -X POST "$base/rules" -H 'Content-Type: application/json' -d '{
  "type":"COUNT_THRESHOLD",
  "keyKind":"cardDevice",
  "window":"PT1H",
  "threshold":5,
  "actionDecision":"BLOCK",
  "actionEntity":"DEVICE",
  "actionTtlSeconds":14400,
  "reason":"CARD_DEVICE_ABUSE",
  "priority":30,
  "enabled":true
}' && echo

echo "Rules loaded."
