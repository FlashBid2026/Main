package com.FlashBid_Main.FlashBid_Main.Auth.handler;

import com.FlashBid_Main.FlashBid_Main.Auth.domain.CustomUserDetails;
import com.FlashBid_Main.FlashBid_Main.Auth.service.JwtTokenProvider;
import com.FlashBid_Main.FlashBid_Main.Auth.util.LocationExtractor;
import com.FlashBid_Main.FlashBid_Main.Auth.util.LocationExtractor.LocationInfo;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final LocationExtractor locationExtractor;

    private final int refreshTokenTtl;
    private final int accessTokenTtl;

    public LoginSuccessHandler(JwtTokenProvider jwtTokenProvider,
                               LocationExtractor locationExtractor,
                               @Value("${jwt.access-expiration}")int accessTokenTtl,
                               @Value("${jwt.refresh-expiration}")int refreshTokenTtl){
        this.jwtTokenProvider = jwtTokenProvider;
        this.locationExtractor = locationExtractor;
        this.refreshTokenTtl = refreshTokenTtl;
        this.accessTokenTtl = accessTokenTtl;
    }


    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        List<String> roles = userDetails.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .toList();

        String accessToken = jwtTokenProvider.createAccessToken(userDetails.getUserId(), userDetails.getNickname(), roles);

        LocationInfo locationInfo = locationExtractor.extractFullLocation(request);
        String refreshToken = jwtTokenProvider.createRefreshToken(userDetails.getUserId(), locationInfo);

        Cookie accessTokenCookie = new Cookie("accessToken", accessToken);
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(accessTokenTtl);

        Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(refreshTokenTtl);

        response.addCookie(accessTokenCookie);
        response.addCookie(refreshTokenCookie);

        response.sendRedirect("/home");
    }
}
