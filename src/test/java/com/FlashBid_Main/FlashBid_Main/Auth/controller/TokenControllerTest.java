package com.FlashBid_Main.FlashBid_Main.Auth.controller;

import com.FlashBid_Main.FlashBid_Main.Auth.service.JwtTokenProvider;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TokenControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @Nested
    @DisplayName("POST /api/auth/refresh")
    class RefreshAccessTokenEndpointTest {

        @Test
        @DisplayName("유효한 refreshToken으로 accessToken 재발행 성공")
        void refreshAccessToken_ValidRefreshToken_Returns200WithAccessTokenCookie() throws Exception {
            String validRefreshToken = "valid-refresh-token";
            String newAccessToken = "new-access-token";

            when(jwtTokenProvider.renewAccessToken(eq(validRefreshToken)))
                .thenReturn(newAccessToken);

            mockMvc.perform(post("/api/auth/refresh")
                    .cookie(new Cookie("refreshToken", validRefreshToken)))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("accessToken"))
                .andExpect(cookie().value("accessToken", newAccessToken))
                .andExpect(cookie().httpOnly("accessToken", true))
                .andExpect(cookie().path("accessToken", "/"));
        }

        @Test
        @DisplayName("refreshToken 쿠키 없음 - 401 Unauthorized")
        void refreshAccessToken_NoRefreshTokenCookie_Returns401() throws Exception {
            mockMvc.perform(post("/api/auth/refresh"))
                .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("유효하지 않은 refreshToken - 401 Unauthorized")
        void refreshAccessToken_InvalidRefreshToken_Returns401() throws Exception {
            String invalidRefreshToken = "invalid-refresh-token";

            when(jwtTokenProvider.renewAccessToken(eq(invalidRefreshToken)))
                .thenThrow(new IllegalArgumentException("Invalid or expired refresh token"));

            mockMvc.perform(post("/api/auth/refresh")
                    .cookie(new Cookie("refreshToken", invalidRefreshToken)))
                .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("만료된 refreshToken - 401 Unauthorized")
        void refreshAccessToken_ExpiredRefreshToken_Returns401() throws Exception {
            String expiredRefreshToken = "expired-refresh-token";

            when(jwtTokenProvider.renewAccessToken(eq(expiredRefreshToken)))
                .thenThrow(new IllegalArgumentException("Invalid or expired refresh token"));

            mockMvc.perform(post("/api/auth/refresh")
                    .cookie(new Cookie("refreshToken", expiredRefreshToken)))
                .andExpect(status().isUnauthorized());
        }
    }
}
