
package com.example.fds.model;

public class Rule {
  public long id;
  public RuleType type;
  public String keyKind;
  public String window;
  public Integer threshold;
  public Double maxKm;
  public Decision actionDecision;
  public ActionEntity actionEntity;
  public Integer actionTtlSeconds;
  public String reason;
  public int priority;
  public boolean enabled;
}
