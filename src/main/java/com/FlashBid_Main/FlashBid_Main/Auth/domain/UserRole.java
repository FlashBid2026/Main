package com.FlashBid_Main.FlashBid_Main.Auth.domain;

import lombok.Getter;

@Getter
public enum UserRole {
  USER("user"), ADMIN("admin");

  private String role;

  UserRole(String roleName){
    this.role = roleName;
  }
}
