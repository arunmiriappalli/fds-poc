package com.example.fds.repo;

import com.example.fds.model.TxnDecision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TxnDecisionRepository extends JpaRepository<TxnDecision, String> {
}
