
package com.example.fds.web;

import com.example.fds.model.Rule;
import com.example.fds.repo.RuleRepository;
import com.example.fds.service.RuleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Comparator;

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
    return repo.findAll().stream().filter(r -> r.enabled).sorted(Comparator.comparingInt(r -> r.priority)).toList();
  }

  @PostMapping
  public ResponseEntity<?> add(@RequestBody Rule r) {
    if (r.reason == null)
      r.reason = "RULE_VIOLATION";
    if (r.priority == 0)
      r.priority = 100;
    if (!r.enabled)
      r.enabled = true;
    r.updatedAt = new java.sql.Timestamp(System.currentTimeMillis());
    repo.save(r);
    rules.reload();
    return ResponseEntity.ok().build();
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<?> delete(@PathVariable long id) {
    repo.deleteById(id);
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
