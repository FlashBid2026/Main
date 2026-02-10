package com.FlashBid_Main.FlashBid_Main.Auction.Controller;

import com.FlashBid_Main.FlashBid_Main.Auth.domain.CustomUserDetails;
import com.FlashBid_Main.FlashBid_Main.Item.dto.ItemDetailResponse;
import com.FlashBid_Main.FlashBid_Main.Item.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
public class AuctionController {

  private final ItemService itemService;

  @GetMapping("/auctions/{id}")
  public String auctionDetail(@PathVariable Long id,
                              @AuthenticationPrincipal CustomUserDetails userDetails,
                              Model model) {
    try {
      ItemDetailResponse auction = itemService.getItemDetail(id);
      model.addAttribute("auction", auction);
      model.addAttribute("userNickname", userDetails != null ? userDetails.getNickname() : "");
      return "auction";
    } catch (IllegalArgumentException e) {
      return "redirect:/home";
    }
  }
}
