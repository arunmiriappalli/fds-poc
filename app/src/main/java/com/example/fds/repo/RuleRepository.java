
package com.example.fds.repo;

import com.example.fds.model.*;
import org.springframework.jdbc.core.*;
import org.springframework.stereotype.Repository;
import java.sql.*;
import java.util.*;

@Repository
public class RuleRepository {
  private final JdbcTemplate jdbc;

  public RuleRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  static class M implements RowMapper<Rule> {
    public Rule mapRow(ResultSet rs, int rn) throws SQLException {
      Rule r = new Rule();
      r.id = rs.getLong("id");
      r.type = RuleType.valueOf(rs.getString("type"));
      r.keyKind = rs.getString("key_kind");
      r.window = rs.getString("time_window");
      int th = rs.getInt("threshold");
      r.threshold = rs.wasNull() ? null : th;
      double mk = rs.getDouble("max_km");
      r.maxKm = rs.wasNull() ? null : mk;
      r.actionDecision = Decision.valueOf(rs.getString("action_decision"));
      r.actionEntity = ActionEntity.valueOf(rs.getString("action_entity"));
      int ttl = rs.getInt("action_ttl_seconds");
      r.actionTtlSeconds = rs.wasNull() ? null : ttl;
      r.reason = rs.getString("reason");
      r.priority = rs.getInt("priority");
      r.enabled = rs.getBoolean("enabled");
      return r;
    }
  }

  public List<Rule> findAllEnabledOrdered() {
    return jdbc.query("SELECT * FROM rules WHERE enabled=true ORDER BY priority ASC, id ASC", new M());
  }

  public long maxUpdatedAtEpochMs() {
    Long v = jdbc.queryForObject("SELECT EXTRACT(EPOCH FROM COALESCE(MAX(updated_at), now()))*1000 FROM rules",
        Long.class);
    return v == null ? 0L : v;
  }

  public int insert(Rule r) {
    String sql = "INSERT INTO rules(type,key_kind,time_window,threshold,max_km,action_decision,action_entity,action_ttl_seconds,reason,priority,enabled) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
    return jdbc.update(sql, r.type.name(), r.keyKind, r.window, r.threshold, r.maxKm, r.actionDecision.name(),
        r.actionEntity.name(), r.actionTtlSeconds, r.reason, r.priority, r.enabled);
  }

  public int delete(long id) {
    return jdbc.update("DELETE FROM rules WHERE id=?", id);
  }

  public int updateEnabled(long id, boolean enabled) {
    return jdbc.update("UPDATE rules SET enabled=?, updated_at=now() WHERE id=?", enabled, id);
  }
}
