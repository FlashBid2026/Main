package com.FlashBid_Main.FlashBid_Main.Bid.controller;

import com.FlashBid_Main.FlashBid_Main.Auth.domain.User;
import com.FlashBid_Main.FlashBid_Main.Bid.dto.BidRequest;
import com.FlashBid_Main.FlashBid_Main.Bid.dto.BidResponse;
import com.FlashBid_Main.FlashBid_Main.Bid.service.BidService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bids")
@RequiredArgsConstructor
public class BidController {

  private final BidService bidService;

  @PostMapping
  public ResponseEntity<BidResponse> placeBid(
      @RequestBody BidRequest request,
      @AuthenticationPrincipal User user
  ) {
    BidResponse response = bidService.placeBid(request, user.getId());
    return ResponseEntity.ok(response);
  }
}