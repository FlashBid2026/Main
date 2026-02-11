package com.FlashBid_Main.FlashBid_Main.Bid.controller;

import com.FlashBid_Main.FlashBid_Main.Auth.domain.CustomUserDetails;
import com.FlashBid_Main.FlashBid_Main.Auth.domain.User;
import com.FlashBid_Main.FlashBid_Main.Auth.domain.UserRole;
import com.FlashBid_Main.FlashBid_Main.Bid.dto.BidRequest;
import com.FlashBid_Main.FlashBid_Main.Bid.dto.BidResponse;
import com.FlashBid_Main.FlashBid_Main.Bid.service.BidService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class BidControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BidService bidService;

    private CustomUserDetails testUserDetails;
    private BidRequest bidRequest;

    @BeforeEach
    void setUp() {
        User testUser = User.builder()
            .userId("test@test.com")
            .password("password123")
            .nickname("testUser")
            .availablePoint(100000L)
            .role(UserRole.USER)
            .build();
        ReflectionTestUtils.setField(testUser, "id", 1L);

        testUserDetails = CustomUserDetails.from(testUser);
        bidRequest = new BidRequest(1L, 15000L);
    }

    private UsernamePasswordAuthenticationToken createAuthentication() {
        return new UsernamePasswordAuthenticationToken(
            testUserDetails,
            null,
            testUserDetails.getAuthorities()
        );
    }

    @Nested
    @DisplayName("POST /api/bids - 인증된 사용자")
    class PlaceBidAuthenticatedTest {

        @Test
        @DisplayName("입찰 성공 시 200 OK 반환")
        void placeBid_Success_Returns200WithSuccessResponse() throws Exception {
            BidResponse successResponse = new BidResponse(true, "입찰에 성공했습니다!", 15000L, "testUser");

            when(bidService.placeBid(any(BidRequest.class), eq("test@test.com")))
                .thenReturn(successResponse);

            mockMvc.perform(post("/api/bids")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(bidRequest))
                    .with(authentication(createAuthentication())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("입찰에 성공했습니다!"))
                .andExpect(jsonPath("$.currentPrice").value(15000))
                .andExpect(jsonPath("$.winnerNickname").value("testUser"));
        }

        @Test
        @DisplayName("성공 응답의 JSON 구조 검증")
        void placeBid_Success_ReturnsCorrectJsonStructure() throws Exception {
            BidResponse successResponse = new BidResponse(true, "입찰에 성공했습니다!", 20000L, "winner");

            when(bidService.placeBid(any(BidRequest.class), eq("test@test.com")))
                .thenReturn(successResponse);

            mockMvc.perform(post("/api/bids")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(new BidRequest(1L, 20000L)))
                    .with(authentication(createAuthentication())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").exists())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.currentPrice").exists())
                .andExpect(jsonPath("$.winnerNickname").exists());
        }

        @Test
        @DisplayName("종료된 경매에 입찰 시 실패 응답 반환")
        void placeBid_AuctionEnded_Returns200WithFailureResponse() throws Exception {
            BidResponse failureResponse = new BidResponse(false, "경매가 이미 종료되었습니다.", 10000L, null);

            when(bidService.placeBid(any(BidRequest.class), eq("test@test.com")))
                .thenReturn(failureResponse);

            mockMvc.perform(post("/api/bids")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(bidRequest))
                    .with(authentication(createAuthentication())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("경매가 이미 종료되었습니다."));
        }

        @Test
        @DisplayName("낮은 금액 입찰 시 실패 응답 반환")
        void placeBid_InvalidBidAmount_Returns200WithFailureResponse() throws Exception {
            BidResponse failureResponse = new BidResponse(false, "현재 최고가보다 높은 금액을 입찰해야 합니다.", 20000L, null);

            when(bidService.placeBid(any(BidRequest.class), eq("test@test.com")))
                .thenReturn(failureResponse);

            mockMvc.perform(post("/api/bids")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(new BidRequest(1L, 15000L)))
                    .with(authentication(createAuthentication())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("현재 최고가보다 높은 금액을 입찰해야 합니다."))
                .andExpect(jsonPath("$.currentPrice").value(20000));
        }
    }

    @Nested
    @DisplayName("POST /api/bids - 예외 처리")
    class PlaceBidExceptionTest {

        @Test
        @DisplayName("상품 미존재 시 에러 반환")
        void placeBid_ItemNotFound_ReturnsError() throws Exception {
            when(bidService.placeBid(any(BidRequest.class), eq("test@test.com")))
                .thenThrow(new IllegalArgumentException("존재하지 않는 상품입니다."));

            mockMvc.perform(post("/api/bids")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(bidRequest))
                    .with(authentication(createAuthentication())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("존재하지 않는 상품입니다."));
        }

        @Test
        @DisplayName("유저 미존재 시 에러 반환")
        void placeBid_UserNotFound_ReturnsError() throws Exception {
            when(bidService.placeBid(any(BidRequest.class), eq("test@test.com")))
                .thenThrow(new IllegalArgumentException("유저를 찾을 수 없습니다."));

            mockMvc.perform(post("/api/bids")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(bidRequest))
                    .with(authentication(createAuthentication())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("유저를 찾을 수 없습니다."));
        }
    }

    @Nested
    @DisplayName("POST /api/bids - 미인증 사용자")
    class PlaceBidUnauthenticatedTest {

        @Test
        @DisplayName("미인증 요청 시 접근 거부")
        void placeBid_Unauthenticated_Returns401Or403() throws Exception {
            mockMvc.perform(post("/api/bids")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(bidRequest)))
                .andExpect(status().is4xxClientError());
        }
    }
}
