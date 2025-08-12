
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
    lastWm.set(repo.maxUpdatedAtEpochMs());
  }

  public List<Rule> rules() {
    return cache.get();
  }

  @Scheduled(fixedDelay = 2000)
  public void tick() {
    long wm = repo.maxUpdatedAtEpochMs();
    if (wm > lastWm.get()) {
      reload();
      lastWm.set(wm);
    }
  }

  public void reload() {
    cache.set(repo.findAllEnabledOrdered());
  }
}
