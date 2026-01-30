package com.FlashBid_Main.FlashBid_Main.Auth.handler;

import com.FlashBid_Main.FlashBid_Main.Auth.domain.CustomUserDetails;
import com.FlashBid_Main.FlashBid_Main.Auth.domain.User;
import com.FlashBid_Main.FlashBid_Main.Auth.domain.UserRole;
import com.FlashBid_Main.FlashBid_Main.Auth.service.JwtTokenProvider;
import com.FlashBid_Main.FlashBid_Main.Auth.util.LocationExtractor;
import com.FlashBid_Main.FlashBid_Main.Auth.util.LocationExtractor.LocationInfo;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LoginSuccessHandlerTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private LocationExtractor locationExtractor;

    private LoginSuccessHandler loginSuccessHandler;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private Authentication authentication;
    private CustomUserDetails userDetails;

    private static final int ACCESS_TOKEN_TTL = 3600;
    private static final int REFRESH_TOKEN_TTL = 86400;

    @BeforeEach
    void setUp() {
        loginSuccessHandler = new LoginSuccessHandler(
            jwtTokenProvider,
            locationExtractor,
            ACCESS_TOKEN_TTL,
            REFRESH_TOKEN_TTL
        );

        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();

        User testUser = User.builder()
            .userId("testuser@example.com")
            .password("encodedPassword")
            .nickname("테스트유저")
            .role(UserRole.USER)
            .build();

        userDetails = CustomUserDetails.from(testUser);
        authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    @Test
    @DisplayName("로그인 성공 시 accessToken 쿠키가 생성된다")
    void onAuthenticationSuccess_CreatesAccessTokenCookie() throws Exception {
        LocationInfo locationInfo = new LocationInfo("127.0.0.1", "Local", "Local");
        given(locationExtractor.extractFullLocation(any())).willReturn(locationInfo);
        given(jwtTokenProvider.createAccessToken(any(), anyString(), anyList())).willReturn("test-access-token");
        given(jwtTokenProvider.createRefreshToken(any(), any(LocationInfo.class))).willReturn("test-refresh-token");

        loginSuccessHandler.onAuthenticationSuccess(request, response, authentication);

        Cookie accessTokenCookie = findCookie(response.getCookies(), "accessToken");
        assertThat(accessTokenCookie).isNotNull();
        assertThat(accessTokenCookie.getValue()).isEqualTo("test-access-token");
    }

    @Test
    @DisplayName("로그인 성공 시 refreshToken 쿠키가 생성된다")
    void onAuthenticationSuccess_CreatesRefreshTokenCookie() throws Exception {
        LocationInfo locationInfo = new LocationInfo("127.0.0.1", "Local", "Local");
        given(locationExtractor.extractFullLocation(any())).willReturn(locationInfo);
        given(jwtTokenProvider.createAccessToken(any(), anyString(), anyList())).willReturn("test-access-token");
        given(jwtTokenProvider.createRefreshToken(any(), any(LocationInfo.class))).willReturn("test-refresh-token");

        loginSuccessHandler.onAuthenticationSuccess(request, response, authentication);

        Cookie refreshTokenCookie = findCookie(response.getCookies(), "refreshToken");
        assertThat(refreshTokenCookie).isNotNull();
        assertThat(refreshTokenCookie.getValue()).isEqualTo("test-refresh-token");
    }

    @Test
    @DisplayName("accessToken 쿠키는 HttpOnly와 올바른 Path가 설정된다")
    void onAuthenticationSuccess_AccessTokenCookie_HasCorrectSettings() throws Exception {
        LocationInfo locationInfo = new LocationInfo("127.0.0.1", "Local", "Local");
        given(locationExtractor.extractFullLocation(any())).willReturn(locationInfo);
        given(jwtTokenProvider.createAccessToken(any(), anyString(), anyList())).willReturn("test-access-token");
        given(jwtTokenProvider.createRefreshToken(any(), any(LocationInfo.class))).willReturn("test-refresh-token");

        loginSuccessHandler.onAuthenticationSuccess(request, response, authentication);

        Cookie accessTokenCookie = findCookie(response.getCookies(), "accessToken");
        assertThat(accessTokenCookie.isHttpOnly()).isTrue();
        assertThat(accessTokenCookie.getPath()).isEqualTo("/");
        assertThat(accessTokenCookie.getMaxAge()).isEqualTo(ACCESS_TOKEN_TTL);
    }

    @Test
    @DisplayName("refreshToken 쿠키는 HttpOnly와 올바른 Path가 설정된다")
    void onAuthenticationSuccess_RefreshTokenCookie_HasCorrectSettings() throws Exception {
        LocationInfo locationInfo = new LocationInfo("127.0.0.1", "Local", "Local");
        given(locationExtractor.extractFullLocation(any())).willReturn(locationInfo);
        given(jwtTokenProvider.createAccessToken(any(), anyString(), anyList())).willReturn("test-access-token");
        given(jwtTokenProvider.createRefreshToken(any(), any(LocationInfo.class))).willReturn("test-refresh-token");

        loginSuccessHandler.onAuthenticationSuccess(request, response, authentication);

        Cookie refreshTokenCookie = findCookie(response.getCookies(), "refreshToken");
        assertThat(refreshTokenCookie.isHttpOnly()).isTrue();
        assertThat(refreshTokenCookie.getPath()).isEqualTo("/");
        assertThat(refreshTokenCookie.getMaxAge()).isEqualTo(REFRESH_TOKEN_TTL);
    }

    @Test
    @DisplayName("로그인 성공 시 /home으로 리다이렉트된다")
    void onAuthenticationSuccess_RedirectsToHome() throws Exception {
        LocationInfo locationInfo = new LocationInfo("127.0.0.1", "Local", "Local");
        given(locationExtractor.extractFullLocation(any())).willReturn(locationInfo);
        given(jwtTokenProvider.createAccessToken(any(), anyString(), anyList())).willReturn("test-access-token");
        given(jwtTokenProvider.createRefreshToken(any(), any(LocationInfo.class))).willReturn("test-refresh-token");

        loginSuccessHandler.onAuthenticationSuccess(request, response, authentication);

        assertThat(response.getRedirectedUrl()).isEqualTo("/home");
    }

    @Test
    @DisplayName("JwtTokenProvider가 올바른 userId와 roles로 호출된다")
    void onAuthenticationSuccess_CallsJwtTokenProviderWithCorrectParameters() throws Exception {
        LocationInfo locationInfo = new LocationInfo("127.0.0.1", "Local", "Local");
        given(locationExtractor.extractFullLocation(any())).willReturn(locationInfo);
        given(jwtTokenProvider.createAccessToken(any(), anyString(), anyList())).willReturn("test-access-token");
        given(jwtTokenProvider.createRefreshToken(any(), any(LocationInfo.class))).willReturn("test-refresh-token");

        loginSuccessHandler.onAuthenticationSuccess(request, response, authentication);

        verify(jwtTokenProvider).createAccessToken(any(), anyString(), anyList());
        verify(jwtTokenProvider).createRefreshToken(any(), any(LocationInfo.class));
    }

    @Test
    @DisplayName("LocationExtractor가 request로부터 위치 정보를 추출한다")
    void onAuthenticationSuccess_ExtractsLocationFromRequest() throws Exception {
        LocationInfo locationInfo = new LocationInfo("127.0.0.1", "Local", "Local");
        given(locationExtractor.extractFullLocation(request)).willReturn(locationInfo);
        given(jwtTokenProvider.createAccessToken(any(), anyString(), anyList())).willReturn("test-access-token");
        given(jwtTokenProvider.createRefreshToken(any(), any(LocationInfo.class))).willReturn("test-refresh-token");

        loginSuccessHandler.onAuthenticationSuccess(request, response, authentication);

        verify(locationExtractor).extractFullLocation(request);
    }

    private Cookie findCookie(Cookie[] cookies, String name) {
        if (cookies == null) {
            return null;
        }
        return Arrays.stream(cookies)
            .filter(cookie -> name.equals(cookie.getName()))
            .findFirst()
            .orElse(null);
    }
}
