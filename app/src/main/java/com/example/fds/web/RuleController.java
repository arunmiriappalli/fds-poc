
package com.example.fds.web;

import com.example.fds.model.Rule;
import com.example.fds.repo.RuleRepository;
import com.example.fds.service.RuleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/rules")
public class RuleController {
  private final RuleRepository repo;
  private final RuleService rules;

  public RuleController(RuleRepository repo, RuleService rules) {
    this.repo = repo;
    this.rules = rules;
  }

  @GetMapping
  public List<Rule> list() {
    return repo.findAllEnabledOrdered();
  }

  @PostMapping
  public ResponseEntity<?> add(@RequestBody Rule r) {
    if (r.reason == null)
      r.reason = "RULE_VIOLATION";
    if (r.priority == 0)
      r.priority = 100;
    if (!r.enabled)
      r.enabled = true;
    repo.insert(r);
    rules.reload();
    return ResponseEntity.ok().build();
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<?> delete(@PathVariable long id) {
    repo.delete(id);
    rules.reload();
    return ResponseEntity.ok().build();
  }

  @PostMapping("/{id}/enabled/{enabled}")
  public ResponseEntity<?> enable(@PathVariable long id, @PathVariable boolean enabled) {
    repo.updateEnabled(id, enabled);
    rules.reload();
    return ResponseEntity.ok().build();
  }
}
