package com.FlashBid_Main.FlashBid_Main.Auth.controller;

import com.FlashBid_Main.FlashBid_Main.Auth.dto.AvailabilityCheckResponse;
import com.FlashBid_Main.FlashBid_Main.Auth.dto.SignupRequest;
import com.FlashBid_Main.FlashBid_Main.Auth.dto.SignupResponse;
import com.FlashBid_Main.FlashBid_Main.Auth.service.SignupService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SignupControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SignupService signupService;

    @Nested
    @DisplayName("POST /api/auth/check-userid")
    class CheckUserIdEndpointTest {

        @Test
        @DisplayName("아이디 중복확인 성공 - 사용 가능한 아이디")
        void checkUserId_Available_Returns200() throws Exception {
            when(signupService.checkUserIdAvailability(eq("newUserId")))
                .thenReturn(AvailabilityCheckResponse.available("사용 가능한 아이디입니다."));

            mockMvc.perform(post("/api/auth/check-userid")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(Map.of("userId", "newUserId"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(true))
                .andExpect(jsonPath("$.message").value("사용 가능한 아이디입니다."));
        }

        @Test
        @DisplayName("아이디 중복확인 - 이미 사용 중인 아이디")
        void checkUserId_Duplicate_ReturnsUnavailable() throws Exception {
            when(signupService.checkUserIdAvailability(eq("existingId")))
                .thenReturn(AvailabilityCheckResponse.unavailable("이미 사용 중인 아이디입니다."));

            mockMvc.perform(post("/api/auth/check-userid")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(Map.of("userId", "existingId"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(false))
                .andExpect(jsonPath("$.message").value("이미 사용 중인 아이디입니다."));
        }

        @Test
        @DisplayName("아이디 중복확인 - 다른 사용자가 선점 중")
        void checkUserId_Locked_ReturnsUnavailable() throws Exception {
            when(signupService.checkUserIdAvailability(eq("lockedId")))
                .thenReturn(AvailabilityCheckResponse.unavailable("다른 사용자가 선점 중인 아이디입니다."));

            mockMvc.perform(post("/api/auth/check-userid")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(Map.of("userId", "lockedId"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(false))
                .andExpect(jsonPath("$.message").value("다른 사용자가 선점 중인 아이디입니다."));
        }
    }

    @Nested
    @DisplayName("POST /api/auth/check-nickname")
    class CheckNicknameEndpointTest {

        @Test
        @DisplayName("닉네임 중복확인 성공 - 사용 가능한 닉네임")
        void checkNickname_Available_Returns200() throws Exception {
            when(signupService.checkNicknameAvailability(eq("newNickname")))
                .thenReturn(AvailabilityCheckResponse.available("사용 가능한 닉네임입니다."));

            mockMvc.perform(post("/api/auth/check-nickname")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(Map.of("nickname", "newNickname"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(true))
                .andExpect(jsonPath("$.message").value("사용 가능한 닉네임입니다."));
        }

        @Test
        @DisplayName("닉네임 중복확인 - 이미 사용 중인 닉네임")
        void checkNickname_Duplicate_ReturnsUnavailable() throws Exception {
            when(signupService.checkNicknameAvailability(eq("existingNick")))
                .thenReturn(AvailabilityCheckResponse.unavailable("이미 사용 중인 닉네임입니다."));

            mockMvc.perform(post("/api/auth/check-nickname")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(Map.of("nickname", "existingNick"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(false))
                .andExpect(jsonPath("$.message").value("이미 사용 중인 닉네임입니다."));
        }

        @Test
        @DisplayName("닉네임 중복확인 - 다른 사용자가 선점 중")
        void checkNickname_Locked_ReturnsUnavailable() throws Exception {
            when(signupService.checkNicknameAvailability(eq("lockedNick")))
                .thenReturn(AvailabilityCheckResponse.unavailable("다른 사용자가 선점 중인 닉네임입니다."));

            mockMvc.perform(post("/api/auth/check-nickname")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(Map.of("nickname", "lockedNick"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(false))
                .andExpect(jsonPath("$.message").value("다른 사용자가 선점 중인 닉네임입니다."));
        }
    }

    @Nested
    @DisplayName("POST /api/auth/signup")
    class SignupEndpointTest {

        @Test
        @DisplayName("회원가입 성공")
        void signup_Success_Returns200() throws Exception {
            SignupRequest request = new SignupRequest(
                "nickname",
                "userId",
                "password123",
                "password123"
            );

            when(signupService.registerUser(any(SignupRequest.class)))
                .thenReturn(SignupResponse.success("회원가입이 완료되었습니다.", "/login"));

            mockMvc.perform(post("/api/auth/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("회원가입이 완료되었습니다."))
                .andExpect(jsonPath("$.redirectUrl").value("/login"));
        }

        @Test
        @DisplayName("회원가입 실패 - 비밀번호 불일치")
        void signup_PasswordMismatch_ReturnsFailure() throws Exception {
            SignupRequest request = new SignupRequest(
                "nickname",
                "userId",
                "password123",
                "differentPassword"
            );

            when(signupService.registerUser(any(SignupRequest.class)))
                .thenReturn(SignupResponse.failure("비밀번호가 일치하지 않습니다."));

            mockMvc.perform(post("/api/auth/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("비밀번호가 일치하지 않습니다."))
                .andExpect(jsonPath("$.redirectUrl").doesNotExist());
        }

        @Test
        @DisplayName("회원가입 유효성 검증 실패 - 아이디 누락")
        void signup_MissingUserId_Returns400() throws Exception {
            Map<String, String> request = Map.of(
                "nickname", "nickname",
                "password", "password123",
                "confirmPassword", "password123"
            );

            mockMvc.perform(post("/api/auth/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("회원가입 유효성 검증 실패 - 닉네임 누락")
        void signup_MissingNickname_Returns400() throws Exception {
            Map<String, String> request = Map.of(
                "userId", "userId",
                "password", "password123",
                "confirmPassword", "password123"
            );

            mockMvc.perform(post("/api/auth/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("회원가입 유효성 검증 실패 - 비밀번호 8자 미만")
        void signup_ShortPassword_Returns400() throws Exception {
            Map<String, String> request = Map.of(
                "nickname", "nickname",
                "userId", "userId",
                "password", "short",
                "confirmPassword", "short"
            );

            mockMvc.perform(post("/api/auth/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("회원가입 실패 - 중복 아이디")
        void signup_DuplicateUserId_ReturnsFailure() throws Exception {
            SignupRequest request = new SignupRequest(
                "nickname",
                "existingId",
                "password123",
                "password123"
            );

            when(signupService.registerUser(any(SignupRequest.class)))
                .thenReturn(SignupResponse.failure("이미 사용 중인 아이디입니다."));

            mockMvc.perform(post("/api/auth/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("이미 사용 중인 아이디입니다."));
        }

        @Test
        @DisplayName("회원가입 실패 - 중복 닉네임")
        void signup_DuplicateNickname_ReturnsFailure() throws Exception {
            SignupRequest request = new SignupRequest(
                "existingNick",
                "userId",
                "password123",
                "password123"
            );

            when(signupService.registerUser(any(SignupRequest.class)))
                .thenReturn(SignupResponse.failure("이미 사용 중인 닉네임입니다."));

            mockMvc.perform(post("/api/auth/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("이미 사용 중인 닉네임입니다."));
        }
    }
}
