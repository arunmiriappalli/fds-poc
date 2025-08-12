
package com.example.fds.repo;

import com.example.fds.model.Rule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface RuleRepository extends JpaRepository<Rule, Long> {

  @Query("SELECT COALESCE(MAX(r.updatedAt), CURRENT_TIMESTAMP) FROM Rule r")
  java.sql.Timestamp maxUpdatedAt();

  @Modifying
  @Transactional
  @Query("UPDATE Rule r SET r.enabled = :enabled, r.updatedAt = CURRENT_TIMESTAMP WHERE r.id = :id")
  int updateEnabled(@Param("id") long id, @Param("enabled") boolean enabled);

  // JpaRepository provides save(), deleteById(), findById(), findAll(), etc.
}
