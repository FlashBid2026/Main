package com.FlashBid_Main.FlashBid_Main.Item.controller;

import com.FlashBid_Main.FlashBid_Main.Auth.domain.CustomUserDetails;
import com.FlashBid_Main.FlashBid_Main.Item.dto.ItemRegistrationDto;
import com.FlashBid_Main.FlashBid_Main.Item.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
public class ItemController {

  private final ItemService itemService;

  @PostMapping("/register")
  public ResponseEntity<String> register(@ModelAttribute ItemRegistrationDto dto,
                                         @AuthenticationPrincipal CustomUserDetails userDetails) {
    try {
      Long itemId = itemService.registerItem(dto, userDetails.getUserId());
      return ResponseEntity.ok("물품 등록 성공! ID: " + itemId);
    } catch (IOException e) {
      return ResponseEntity.status(500).body("등록 실패: " + e.getMessage());
    }
  }
}
