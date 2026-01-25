package com.FlashBid_Main.FlashBid_Main.User.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
public class User {

  @Id
  @GeneratedValue
  private long id;

  private String userId;

  private String password;

}
