package com.example.fds.web;

import com.example.fds.dto.DecisionResponse;
import com.example.fds.dto.TxnRequest;
import com.example.fds.model.ActionEntity;
import com.example.fds.model.Decision;
import com.example.fds.model.Rule;
import com.example.fds.model.RuleType;
import com.example.fds.repo.CardRepository;
import com.example.fds.service.RedisWindows;
import com.example.fds.service.RuleService;
import com.example.fds.util.Geo;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.springframework.dao.DataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/transactions")
public class TxnController {
    private final RuleService rules;
    private final RedisWindows redis;
    private final CardRepository cards;
    private final JdbcTemplate jdbc;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public TxnController(RuleService rules, RedisWindows redis, CardRepository cards, JdbcTemplate jdbc) {
        this.rules = rules;
        this.redis = redis;
        this.cards = cards;
        this.jdbc = jdbc;
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
                CardRepository.CardLoc loc = cards.findRegLoc(req.card_id);
                if (loc != null && r.maxKm != null) {
                    double dkm = Geo.haversineKm(req.lat, req.lon, loc.lat, loc.lon);
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
            String json = "{}";
            try {
                json = MAPPER.writeValueAsString(details == null ? Map.of() : details);
            } catch (Exception ignore) {
            }
            jdbc.update(
                    "INSERT INTO txn_decisions(txn_id, decision, reason, matched_rule_id, details) VALUES (?,?,?,?, ?::jsonb)",
                    req.txn_id, dr.decision.name(), dr.reason, dr.matched_rule_id, json);
        } catch (DataAccessException ignored) {
        }
    }
}