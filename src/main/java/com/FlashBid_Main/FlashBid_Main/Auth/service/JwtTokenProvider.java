package com.FlashBid_Main.FlashBid_Main.Auth.service;

import com.FlashBid_Main.FlashBid_Main.Auth.domain.RefreshToken;
import com.FlashBid_Main.FlashBid_Main.Auth.domain.TokenType;
import com.FlashBid_Main.FlashBid_Main.Auth.domain.User;
import com.FlashBid_Main.FlashBid_Main.Auth.repository.UserRepository;
import com.FlashBid_Main.FlashBid_Main.Auth.util.LocationExtractor.LocationInfo;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long accessExpiration;
    private final long refreshExpiration;
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;

    public JwtTokenProvider(
        @Value("${jwt.secret}") String key,
        @Value("${jwt.access-expiration}") long accessExpiration,
        @Value("${jwt.refresh-expiration}") long refreshExpiration,
        RefreshTokenService refreshTokenService,
        UserRepository userRepository
    ) {
        this.secretKey = Keys.hmacShaKeyFor(key.getBytes(StandardCharsets.UTF_8));
        this.accessExpiration = accessExpiration;
        this.refreshExpiration = refreshExpiration;
        this.refreshTokenService = refreshTokenService;
        this.userRepository = userRepository;
    }

    public String createAccessToken(String userId, String nickname, List<String> roles) {
        return createToken(userId, nickname, roles, TokenType.ACCESS, accessExpiration);
    }

    public String createRefreshToken(String userId, LocationInfo locationInfo) {
        String userIdStr = userId;
        String token = createToken(userIdStr, null, null, TokenType.REFRESH, refreshExpiration);

        RefreshToken refreshToken = new RefreshToken(
            userIdStr,
            token,
            locationInfo.ipAddress(),
            locationInfo.country(),
            locationInfo.city(),
            refreshExpiration
        );

        refreshTokenService.save(refreshToken);

        return token;
    }

    private String createToken(String userId, String nickname, List<String> roles, TokenType tokenType, long validity) {
        Date now = new Date();
        Date expireDate = new Date(now.getTime() + validity);

        JwtBuilder builder = Jwts.builder()
            .header()
                .add("typ", "JWT")
                .and()
            .subject(userId)
            .claim("token_type", tokenType.getValue())
            .issuedAt(now)
            .expiration(expireDate)
            .signWith(secretKey);

        if (tokenType == TokenType.ACCESS) {
            if (nickname != null) {
                builder.claim("nickname", nickname);
            }
            if (roles != null) {
                builder.claim("roles", roles);
            }
        }

        return builder.compact();
    }

    public boolean validToken(String token) {
        try {
            Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }


    public boolean validRefreshToken(String token) {
        try {
            Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

            String tokenType = claims.get("token_type", String.class);
            if (!TokenType.REFRESH.getValue().equals(tokenType)) {
                return false;
            }

            return refreshTokenService.findByToken(token).isPresent();

        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public boolean validAccessToken(String token) {
        try {
            Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

            String tokenType = claims.get("token_type", String.class);
            return TokenType.ACCESS.getValue().equals(tokenType);

        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String renewAccessToken(String refreshToken) {
        if (!validRefreshToken(refreshToken)) {
            throw new IllegalArgumentException("Invalid or expired refresh token");
        }

        String userId = getUserId(refreshToken);

        RefreshToken storedToken = refreshTokenService.findByToken(refreshToken)
            .orElseThrow(() -> new IllegalArgumentException("Refresh token not found in storage"));

        if (!userId.equals(storedToken.getUserId())) {
            throw new IllegalArgumentException("Token user mismatch");
        }

        User user = userRepository.findByUserId(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        List<String> roles = List.of(user.getRole().name());

        return createAccessToken(userId, user.getNickname(), roles);
    }

    public void revokeRefreshToken(String userId) {
        refreshTokenService.deleteByUserId(userId);
    }

    public String getUserId(String token) {
        return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .getPayload()
            .getSubject();
    }

    public List<String> getRoles(String token) {
        return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .getPayload()
            .get("roles", List.class);
    }

    public String getNickname(String token) {
        return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .getPayload()
            .get("nickname", String.class);
    }

    public TokenType getTokenType(String token) {
        String type = Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .getPayload()
            .get("token_type", String.class);

        return TokenType.valueOf(type.toUpperCase());
    }
}
