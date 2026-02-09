package com.FlashBid_Main.FlashBid_Main.Auth.controller;

import com.FlashBid_Main.FlashBid_Main.Auth.domain.CustomUserDetails;
import com.FlashBid_Main.FlashBid_Main.Auth.service.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RankingTokenControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    private UsernamePasswordAuthenticationToken createAuthentication() {
        CustomUserDetails userDetails = new CustomUserDetails(
                "test@example.com",
                "password",
                "testuser",
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        return new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );
    }

    @Nested
    @DisplayName("POST /api/ranking/token - 인증된 사용자")
    class IssueRankingTokenAuthenticatedTest {

        @Test
        @DisplayName("유효한 요청으로 rankingToken 쿠키 발급 성공")
        void issueRankingToken_ValidRequest_Returns200WithCookie() throws Exception {
            String roomId = "auction-room-123";
            String rankingToken = "ranking-jwt-token";

            when(jwtTokenProvider.createRankingToken(
                    eq("test@example.com"), eq("testuser"), eq(roomId)))
                    .thenReturn(rankingToken);

            mockMvc.perform(post("/api/ranking/token")
                            .param("roomId", roomId)
                            .with(authentication(createAuthentication())))
                    .andExpect(status().isOk())
                    .andExpect(cookie().exists("rankingToken"))
                    .andExpect(cookie().value("rankingToken", rankingToken))
                    .andExpect(cookie().httpOnly("rankingToken", true))
                    .andExpect(cookie().path("rankingToken", "/"));
        }

        @Test
        @DisplayName("roomId 파라미터 누락 시 400 Bad Request")
        void issueRankingToken_MissingRoomId_Returns400() throws Exception {
            mockMvc.perform(post("/api/ranking/token")
                            .with(authentication(createAuthentication())))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("POST /api/ranking/token - 미인증 사용자")
    class IssueRankingTokenUnauthenticatedTest {

        @Test
        @DisplayName("인증되지 않은 요청 시 401 Unauthorized")
        void issueRankingToken_Unauthenticated_Returns401() throws Exception {
            mockMvc.perform(post("/api/ranking/token")
                            .param("roomId", "auction-room-123"))
                    .andExpect(status().isUnauthorized());
        }
    }
}
