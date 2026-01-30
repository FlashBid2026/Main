package com.FlashBid_Main.FlashBid_Main.Auth.filter;

import com.FlashBid_Main.FlashBid_Main.Auth.service.JwtTokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class JwtFilterTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtFilter jwtFilter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("유효한 accessToken 쿠키가 있으면 SecurityContext에 Authentication이 설정됨")
    void validAccessToken_SetsAuthentication() throws Exception {
        String token = "valid.access.token";
        String userId = "123";
        List<String> roles = List.of("ROLE_USER");

        request.setCookies(new Cookie("accessToken", token));

        given(jwtTokenProvider.validAccessToken(token)).willReturn(true);
        given(jwtTokenProvider.getUserId(token)).willReturn(userId);
        given(jwtTokenProvider.getRoles(token)).willReturn(roles);

        jwtFilter.doFilterInternal(request, response, filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        assertThat(authentication.getPrincipal()).isEqualTo(userId);
        assertThat(authentication.getCredentials()).isNull();
        assertThat(authentication.getAuthorities())
            .extracting(GrantedAuthority::getAuthority)
            .containsExactly("ROLE_USER");

        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("쿠키가 없으면 SecurityContext는 비어있음")
    void noCookies_SecurityContextEmpty() throws Exception {
        jwtFilter.doFilterInternal(request, response, filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNull();

        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("유효하지 않은 토큰이면 SecurityContext는 비어있음")
    void invalidToken_SecurityContextEmpty() throws Exception {
        String token = "invalid.token";
        request.setCookies(new Cookie("accessToken", token));

        given(jwtTokenProvider.validAccessToken(token)).willReturn(false);

        jwtFilter.doFilterInternal(request, response, filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNull();

        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("accessToken이 아닌 다른 쿠키만 있으면 SecurityContext는 비어있음")
    void otherCookiesOnly_SecurityContextEmpty() throws Exception {
        request.setCookies(new Cookie("otherCookie", "someValue"));

        jwtFilter.doFilterInternal(request, response, filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNull();

        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("여러 권한이 올바르게 설정됨")
    void multipleRoles_AuthoritiesSetCorrectly() throws Exception {
        String token = "valid.access.token";
        String userId = "456";
        List<String> roles = List.of("ROLE_USER", "ROLE_ADMIN");

        request.setCookies(new Cookie("accessToken", token));

        given(jwtTokenProvider.validAccessToken(token)).willReturn(true);
        given(jwtTokenProvider.getUserId(token)).willReturn(userId);
        given(jwtTokenProvider.getRoles(token)).willReturn(roles);

        jwtFilter.doFilterInternal(request, response, filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        assertThat(authentication.getAuthorities())
            .extracting(GrantedAuthority::getAuthority)
            .containsExactly("ROLE_USER", "ROLE_ADMIN");
        verify(filterChain).doFilter(request, response);
    }
}
