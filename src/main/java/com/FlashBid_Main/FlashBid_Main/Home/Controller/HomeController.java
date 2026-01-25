package com.FlashBid_Main.FlashBid_Main.Home.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

  @GetMapping("/")
  public String homePage(){

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
