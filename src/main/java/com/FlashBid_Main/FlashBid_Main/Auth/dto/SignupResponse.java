package com.FlashBid_Main.FlashBid_Main.Auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SignupResponse {

  private boolean success;
  private String message;
  private String redirectUrl;

  public static SignupResponse success(String message, String redirectUrl) {
    return new SignupResponse(true, message, redirectUrl);
  }

  public static SignupResponse failure(String message) {
    return new SignupResponse(false, message, null);
  }
}
