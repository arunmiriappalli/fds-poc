

package com.example.fds.model;

import jakarta.persistence.*;

@Entity
@Table(name = "rules")
public class Rule {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  public RuleType type;

  @Column(name = "key_kind")
  public String keyKind;

  @Column(name = "time_window")
  public String window;

  public Integer threshold;

  @Column(name = "max_km")
  public Double maxKm;

  @Enumerated(EnumType.STRING)
  @Column(name = "action_decision", nullable = false)
  public Decision actionDecision;

  @Enumerated(EnumType.STRING)
  @Column(name = "action_entity", nullable = false)
  public ActionEntity actionEntity;

  @Column(name = "action_ttl_seconds")
  public Integer actionTtlSeconds;

  public String reason;

  public int priority;

  public boolean enabled;

  @Column(name = "updated_at")
  public java.sql.Timestamp updatedAt;
}
