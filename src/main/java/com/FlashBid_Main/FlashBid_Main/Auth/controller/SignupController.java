package com.FlashBid_Main.FlashBid_Main.Auth.controller;

import com.FlashBid_Main.FlashBid_Main.Auth.dto.AvailabilityCheckResponse;
import com.FlashBid_Main.FlashBid_Main.Auth.dto.SignupRequest;
import com.FlashBid_Main.FlashBid_Main.Auth.dto.SignupResponse;
import com.FlashBid_Main.FlashBid_Main.Auth.service.SignupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class SignupController {

  private final SignupService signupService;

  @PostMapping("/check-userid")
  public ResponseEntity<AvailabilityCheckResponse> checkUserId(@RequestBody Map<String, String> request) {
    String userId = request.get("userId");
    AvailabilityCheckResponse response = signupService.checkUserIdAvailability(userId);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/check-nickname")
  public ResponseEntity<AvailabilityCheckResponse> checkNickname(@RequestBody Map<String, String> request) {
    String nickname = request.get("nickname");
    AvailabilityCheckResponse response = signupService.checkNicknameAvailability(nickname);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/signup")
  public ResponseEntity<SignupResponse> signup(@Valid @RequestBody SignupRequest request) {
    SignupResponse response = signupService.registerUser(request);
    return ResponseEntity.ok(response);
  }
}
