package com.FlashBid_Main.FlashBid_Main.Auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AvailabilityCheckResponse {

  private boolean available;
  private String message;

  public static AvailabilityCheckResponse available(String message) {
    return new AvailabilityCheckResponse(true, message);
  }

  public static AvailabilityCheckResponse unavailable(String message) {
    return new AvailabilityCheckResponse(false, message);
  }
}
