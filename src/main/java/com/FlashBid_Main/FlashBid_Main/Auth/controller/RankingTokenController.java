package com.FlashBid_Main.FlashBid_Main.Auth.controller;

import com.FlashBid_Main.FlashBid_Main.Auth.domain.CustomUserDetails;
import com.FlashBid_Main.FlashBid_Main.Auth.service.JwtTokenProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auction")
@RequiredArgsConstructor
public class RankingTokenController {

    private final JwtTokenProvider jwtTokenProvider;

    @Value("${jwt.access-expiration}")
    private int accessTokenTtl;

    @PostMapping("/ranking-token")
    public ResponseEntity<Void> issueRankingToken(
            @RequestParam String roomId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletResponse response) {

        String token = jwtTokenProvider.createRankingToken(
                userDetails.getUserId(),
                userDetails.getNickname(),
                roomId
        );

        Cookie rankingTokenCookie = new Cookie("rankingToken", token);
        rankingTokenCookie.setHttpOnly(true);
        rankingTokenCookie.setPath("/");
        rankingTokenCookie.setMaxAge(accessTokenTtl / 1000);

        response.addCookie(rankingTokenCookie);

        return ResponseEntity.ok().build();
    }
}
