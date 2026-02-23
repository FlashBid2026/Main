package com.FlashBid_Main.FlashBid_Main.Notification.controller;

import com.FlashBid_Main.FlashBid_Main.Auth.domain.CustomUserDetails;
import com.FlashBid_Main.FlashBid_Main.Notification.dto.NotificationResponse;
import com.FlashBid_Main.FlashBid_Main.Notification.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationService notificationService;

    private NotificationResponse sampleResponse;

    @BeforeEach
    void setUp() {
        sampleResponse = new NotificationResponse(
            1L, "SUCCESS", "낙찰 완료!", "[테스트 상품]에 15,000원으로 낙찰되셨습니다",
            "/auctions/10", false, LocalDateTime.now()
        );
    }

    private Authentication createAuthentication() {
        CustomUserDetails userDetails = new CustomUserDetails(
            "testUser", null, "TestNick",
            List.of(new SimpleGrantedAuthority("ROLE_USER")));
        return new UsernamePasswordAuthenticationToken(
            userDetails, null, userDetails.getAuthorities());
    }

    @Nested
    @DisplayName("GET /api/notifications")
    class GetAllNotificationsTest {

        @Test
        @DisplayName("인증된 사용자 - 200 OK 및 알림 목록 반환")
        void getAll_Authenticated_Returns200WithList() throws Exception {
            when(notificationService.getNotificationsForUser("testUser"))
                .thenReturn(List.of(sampleResponse));

            mockMvc.perform(get("/api/notifications")
                    .with(authentication(createAuthentication())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type").value("SUCCESS"))
                .andExpect(jsonPath("$[0].title").value("낙찰 완료!"))
                .andExpect(jsonPath("$[0].read").value(false));
        }

        @Test
        @DisplayName("미인증 요청 - 4xx 반환")
        void getAll_Unauthenticated_Returns4xx() throws Exception {
            mockMvc.perform(get("/api/notifications"))
                .andExpect(status().is4xxClientError());
        }
    }

    @Nested
    @DisplayName("GET /api/notifications/unread")
    class GetUnreadNotificationsTest {

        @Test
        @DisplayName("인증된 사용자 - 200 OK 및 미읽음 알림 목록 반환")
        void getUnread_Authenticated_Returns200WithUnreadList() throws Exception {
            when(notificationService.getUnreadNotificationsForUser("testUser"))
                .thenReturn(List.of(sampleResponse));

            mockMvc.perform(get("/api/notifications/unread")
                    .with(authentication(createAuthentication())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type").value("SUCCESS"))
                .andExpect(jsonPath("$[0].read").value(false));
        }

        @Test
        @DisplayName("미인증 요청 - 4xx 반환")
        void getUnread_Unauthenticated_Returns4xx() throws Exception {
            mockMvc.perform(get("/api/notifications/unread"))
                .andExpect(status().is4xxClientError());
        }
    }

    @Nested
    @DisplayName("PATCH /api/notifications/{id}/read")
    class MarkAsReadTest {

        @Test
        @DisplayName("인증된 사용자 - 200 OK 및 markAsRead 서비스 호출 확인")
        void markAsRead_Authenticated_Returns200AndCallsService() throws Exception {
            mockMvc.perform(patch("/api/notifications/1/read")
                    .with(authentication(createAuthentication())))
                .andExpect(status().isOk());

            verify(notificationService).markAsRead(eq(1L), eq("testUser"));
        }

        @Test
        @DisplayName("미인증 요청 - 4xx 반환")
        void markAsRead_Unauthenticated_Returns4xx() throws Exception {
            mockMvc.perform(patch("/api/notifications/1/read"))
                .andExpect(status().is4xxClientError());
        }
    }
}
