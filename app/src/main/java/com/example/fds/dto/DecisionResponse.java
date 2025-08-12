
package com.example.fds.dto;

import com.example.fds.model.Decision;

public class DecisionResponse {
  public Decision decision;
  public String reason;
  public Long matched_rule_id;

  public static DecisionResponse of(Decision d, String reason, Long ruleId) {
    DecisionResponse r = new DecisionResponse();
    r.decision = d;
    r.reason = reason;
    r.matched_rule_id = ruleId;
    return r;
  }
}
