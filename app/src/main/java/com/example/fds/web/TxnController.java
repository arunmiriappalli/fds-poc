package com.example.fds.web;

import com.example.fds.dto.DecisionResponse;
import com.example.fds.dto.TxnRequest;
import com.example.fds.model.ActionEntity;
import com.example.fds.model.Decision;
import com.example.fds.model.Device;
import com.example.fds.model.Rule;
import com.example.fds.model.RuleType;
import com.example.fds.repo.DeviceRepository;
import com.example.fds.service.RedisWindows;
import com.example.fds.service.RuleService;
import com.example.fds.util.Geo;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import com.example.fds.model.TxnDecision;
import com.example.fds.repo.TxnDecisionRepository;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/transactions")
public class TxnController {
    private final RuleService rules;
    private final RedisWindows redis;
    private final DeviceRepository devices;
    private final TxnDecisionRepository txnDecisionRepo;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public TxnController(RuleService rules, RedisWindows redis, DeviceRepository devices, TxnDecisionRepository txnDecisionRepo) {
        this.rules = rules;
        this.redis = redis;
        this.devices = devices;
        this.txnDecisionRepo = txnDecisionRepo;
    }

    @PostMapping("/authorize")
    public ResponseEntity<DecisionResponse> authorize(@Valid @RequestBody TxnRequest req) {
        long now = Instant.now().toEpochMilli();

        if (redis.isBlocked(blockKeyDevice(req.device_id))) {
            DecisionResponse dr = DecisionResponse.of(Decision.BLOCK, "BLOCKLIST_DEVICE", null);
            writeDecision(req, dr, null);
            return ResponseEntity.ok(dr);
        }
        if (redis.isBlocked(blockKeyCard(req.card_id))) {
            DecisionResponse dr = DecisionResponse.of(Decision.BLOCK, "BLOCKLIST_CARD", null);
            writeDecision(req, dr, null);
            return ResponseEntity.ok(dr);
        }

        List<Rule> list = rules.rules();
        Map<String, Object> details = new HashMap<>();

        for (Rule r : list) {
            boolean violated = false;
            if (r.type == RuleType.GEO_DISTANCE) {
                Optional<Device> device = devices.findById(req.device_id);
                if (device.isPresent() && device.get().regLat != null && device.get().regLon != null && r.maxKm != null) {
                    double dkm = Geo.haversineKm(req.lat, req.lon, device.get().regLat, device.get().regLon);
                    details.put("geo_km", dkm);
                    if (dkm > r.maxKm)
                        violated = true;
                }
            } else if (r.type == RuleType.COUNT_THRESHOLD) {
                String key = null;
                if ("device".equalsIgnoreCase(r.keyKind)) {
                    key = "win:device:" + req.device_id;
                } else if ("cardDevice".equalsIgnoreCase(r.keyKind)) {
                    key = "win:cardDevice:" + req.card_id + ":" + req.device_id;
                }
                if (key != null && r.threshold != null && r.window != null) {
                    long count = redis.countAndTrim(key, now, Duration.parse(r.window), req.txn_id);
                    details.put("count:" + key, count);
                    if (count > r.threshold)
                        violated = true;
                }
            }

            if (violated) {
                if (r.actionEntity == ActionEntity.DEVICE && r.actionTtlSeconds != null) {
                    redis.blockKey(blockKeyDevice(req.device_id), Duration.ofSeconds(r.actionTtlSeconds));
                } else if (r.actionEntity == ActionEntity.CARD && r.actionTtlSeconds != null) {
                    redis.blockKey(blockKeyCard(req.card_id), Duration.ofSeconds(r.actionTtlSeconds));
                }
                DecisionResponse dr = DecisionResponse.of(r.actionDecision, r.reason, r.id);
                writeDecision(req, dr, details);
                return ResponseEntity.ok(dr);
            }
        }

        DecisionResponse dr = DecisionResponse.of(Decision.ALLOW, "OK", null);
        writeDecision(req, dr, details);
        return ResponseEntity.ok(dr);
    }

    private static String blockKeyDevice(String deviceId) {
        return "block:device:" + deviceId;
    }

    private static String blockKeyCard(String cardId) {
        return "block:card:" + cardId;
    }

    private void writeDecision(TxnRequest req, DecisionResponse dr, Map<String, Object> details) {
        try {
            com.fasterxml.jackson.databind.JsonNode jsonNode = MAPPER.valueToTree(details == null ? Map.of() : details);
            TxnDecision td = new TxnDecision();
            td.txnId = req.txn_id;
            td.ts = java.sql.Timestamp.from(java.time.Instant.now());
            td.decision = dr.decision.name();
            td.reason = dr.reason;
            td.matchedRuleId = dr.matched_rule_id;
            td.details = jsonNode;
            txnDecisionRepo.save(td);
        } catch (Exception ignored) {
        }
    }
}