package com.example.fds.model;

import jakarta.persistence.*;
import java.sql.Timestamp;
import com.vladmihalcea.hibernate.type.json.JsonType;
import org.hibernate.annotations.Type;
import com.fasterxml.jackson.databind.JsonNode;

@Entity
@Table(name = "txn_decisions")
public class TxnDecision {
    @Id
    @Column(name = "txn_id")
    public String txnId;

    @Column(name = "ts")
    public Timestamp ts;

    @Column(name = "decision")
    public String decision;

    @Column(name = "reason")
    public String reason;

    @Column(name = "matched_rule_id")
    public Long matchedRuleId;

    @Type(JsonType.class)
    @Column(name = "details", columnDefinition = "jsonb")
    public JsonNode details;
}
