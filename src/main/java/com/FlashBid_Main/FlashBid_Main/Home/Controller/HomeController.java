package com.FlashBid_Main.FlashBid_Main.Home.Controller;

import com.FlashBid_Main.FlashBid_Main.Item.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {

  private final ItemService itemService;

  @GetMapping("/")
  public String homePage(Model model){
    model.addAttribute("auctions", itemService.getActiveItemsForHome());
    return "home";
  }

  @GetMapping("/home")
  public String home(Model model){
    model.addAttribute("auctions", itemService.getActiveItemsForHome());
    return "home";
  }

  @GetMapping("/login")
  public String loginPage(){
    return "login";
  }

  @GetMapping("/signup")
  public String signupPage(){
    return "signup";
  }
}
