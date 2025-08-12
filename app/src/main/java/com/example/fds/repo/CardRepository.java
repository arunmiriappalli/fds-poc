
package com.example.fds.repo;

import com.example.fds.model.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CardRepository extends JpaRepository<Card, String> {

  @Query("SELECT c FROM Card c WHERE c.cardId = :cardId")
  Card findByCardId(@Param("cardId") String cardId);

}
