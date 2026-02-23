package com.FlashBid_Main.FlashBid_Main.Notification.controller;

import com.FlashBid_Main.FlashBid_Main.Auth.domain.CustomUserDetails;
import com.FlashBid_Main.FlashBid_Main.Notification.dto.NotificationResponse;
import com.FlashBid_Main.FlashBid_Main.Notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class MessageController {

    private final NotificationService notificationService;

    @GetMapping("/messages")
    public String messagesPage(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        Model model) {
        List<NotificationResponse> notifications =
            notificationService.getNotificationsForUser(userDetails.getUserId());
        model.addAttribute("messages", notifications);
        return "messages";
    }
}
