
package com.example.fds.service;

import com.example.fds.model.Rule;
import com.example.fds.repo.RuleRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.concurrent.atomic.*;

@Service
public class RuleService {
  private final RuleRepository repo;
  private final AtomicReference<List<Rule>> cache = new AtomicReference<>(List.of());
  private final AtomicLong lastWm = new AtomicLong(0);

  public RuleService(RuleRepository repo) {
    this.repo = repo;
    reload();
    lastWm.set(tsToEpochMs(repo.maxUpdatedAt()));
  }

  public List<Rule> rules() {
    return cache.get();
  }

  @Scheduled(fixedDelay = 2000)
  public void tick() {
    long wm = tsToEpochMs(repo.maxUpdatedAt());
    if (wm > lastWm.get()) {
      reload();
      lastWm.set(wm);
    }
  }

  public void reload() {
    cache.set(repo.findAll().stream().filter(r -> r.enabled).sorted(Comparator.comparingInt(r -> r.priority)).toList());
  }

  private long tsToEpochMs(java.sql.Timestamp ts) {
    return ts == null ? 0L : ts.getTime();
  }
}
