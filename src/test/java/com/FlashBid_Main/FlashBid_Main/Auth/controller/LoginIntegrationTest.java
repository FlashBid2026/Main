package com.FlashBid_Main.FlashBid_Main.Auth.controller;

import com.FlashBid_Main.FlashBid_Main.Auth.domain.User;
import com.FlashBid_Main.FlashBid_Main.Auth.domain.UserRole;
import com.FlashBid_Main.FlashBid_Main.Auth.repository.UserRepository;
import com.FlashBid_Main.FlashBid_Main.Auth.service.RefreshTokenService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class LoginIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RefreshTokenService refreshTokenService;

    private User testUser;
    private static final String TEST_USER_ID = "logintest@example.com";
    private static final String TEST_PASSWORD = "testPassword123";
    private static final String TEST_NICKNAME = "로그인테스트";

    @BeforeEach
    void setUp() {
        testUser = User.builder()
            .userId(TEST_USER_ID)
            .password(passwordEncoder.encode(TEST_PASSWORD))
            .nickname(TEST_NICKNAME)
            .role(UserRole.USER)
            .build();
        testUser = userRepository.save(testUser);
    }

    @AfterEach
    void tearDown() {
        refreshTokenService.deleteByUserId(String.valueOf(testUser.getId()));
        userRepository.deleteAll();
    }

    @Nested
    @DisplayName("GET /login - 로그인 페이지 테스트")
    class LoginPageTest {
        @Test
        @DisplayName("로그인 페이지 GET 요청이 성공한다")
        void getLoginPage_Returns200() throws Exception {
            mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
        }
    }

    @Nested
    @DisplayName("POST /login - 로그인 처리 테스트")
    class LoginProcessTest {

        @Test
        @DisplayName("올바른 자격 증명으로 로그인 시 /home으로 리다이렉트된다")
        void login_WithValidCredentials_RedirectsToHome() throws Exception {
            mockMvc.perform(post("/login")
                    .param("userId", TEST_USER_ID)
                    .param("password", TEST_PASSWORD))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home"));
        }

        @Test
        @DisplayName("올바른 로그인 후 응답에 accessToken 쿠키가 포함된다")
        void login_WithValidCredentials_ContainsAccessTokenCookie() throws Exception {
            MvcResult result = mockMvc.perform(post("/login")
                    .param("userId", TEST_USER_ID)
                    .param("password", TEST_PASSWORD))
                .andExpect(status().is3xxRedirection())
                .andReturn();

            Cookie accessTokenCookie = result.getResponse().getCookie("accessToken");
            assertThat(accessTokenCookie).isNotNull();
            assertThat(accessTokenCookie.getValue()).isNotEmpty();
            assertThat(accessTokenCookie.isHttpOnly()).isTrue();
            assertThat(accessTokenCookie.getPath()).isEqualTo("/");
        }

        @Test
        @DisplayName("올바른 로그인 후 응답에 refreshToken 쿠키가 포함된다")
        void login_WithValidCredentials_ContainsRefreshTokenCookie() throws Exception {
            MvcResult result = mockMvc.perform(post("/login")
                    .param("userId", TEST_USER_ID)
                    .param("password", TEST_PASSWORD))
                .andExpect(status().is3xxRedirection())
                .andReturn();

            Cookie refreshTokenCookie = result.getResponse().getCookie("refreshToken");
            assertThat(refreshTokenCookie).isNotNull();
            assertThat(refreshTokenCookie.getValue()).isNotEmpty();
            assertThat(refreshTokenCookie.isHttpOnly()).isTrue();
            assertThat(refreshTokenCookie.getPath()).isEqualTo("/");
        }

        @Test
        @DisplayName("잘못된 비밀번호로 로그인 시 실패한다")
        void login_WithWrongPassword_Fails() throws Exception {
            mockMvc.perform(post("/login")
                    .param("userId", TEST_USER_ID)
                    .param("password", "wrongPassword"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/login?error*"));
        }

        @Test
        @DisplayName("존재하지 않는 사용자로 로그인 시 실패한다")
        void login_WithNonExistingUser_Fails() throws Exception {
            mockMvc.perform(post("/login")
                    .param("userId", "nonexistent@example.com")
                    .param("password", TEST_PASSWORD))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/login?error*"));
        }

        @Test
        @DisplayName("빈 userId로 로그인 시 실패한다")
        void login_WithEmptyUserId_Fails() throws Exception {
            mockMvc.perform(post("/login")
                    .param("userId", "")
                    .param("password", TEST_PASSWORD))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/login?error*"));
        }

        @Test
        @DisplayName("빈 password로 로그인 시 실패한다")
        void login_WithEmptyPassword_Fails() throws Exception {
            mockMvc.perform(post("/login")
                    .param("userId", TEST_USER_ID)
                    .param("password", ""))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/login?error*"));
        }
    }

    @Nested
    @DisplayName("로그아웃 테스트")
    class LogoutTest {
        @Test
        @DisplayName("로그아웃 시 /home으로 리다이렉트된다")
        void logout_RedirectsToHome() throws Exception {
            mockMvc.perform(post("/logout"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home"));
        }
    }
}