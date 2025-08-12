
package com.example.fds.repo;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class CardRepository {
  private final JdbcTemplate jdbc;

  public CardRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public static class CardLoc {
    public final double lat, lon;

    public CardLoc(double lat, double lon) {
      this.lat = lat;
      this.lon = lon;
    }
  }

  public CardLoc findRegLoc(String cardId) {
    return jdbc.query("SELECT reg_lat, reg_lon FROM cards WHERE card_id=?",
        rs -> rs.next() ? new CardLoc(rs.getDouble(1), rs.getDouble(2)) : null, cardId);
  }
}
