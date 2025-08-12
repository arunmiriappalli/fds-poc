package com.example.fds.model;

import jakarta.persistence.*;

@Entity
@Table(name = "cards")
public class Card {
  @Id
  @Column(name = "card_id")
  public String cardId;

  @Column(name = "reg_lat")
  public Double regLat;

  @Column(name = "reg_lon")
  public Double regLon;

}
