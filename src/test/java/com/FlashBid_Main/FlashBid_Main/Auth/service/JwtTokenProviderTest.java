package com.FlashBid_Main.FlashBid_Main.Auth.service;

import com.FlashBid_Main.FlashBid_Main.Auth.domain.RefreshToken;
import com.FlashBid_Main.FlashBid_Main.Auth.domain.TokenType;
import com.FlashBid_Main.FlashBid_Main.Auth.domain.User;
import com.FlashBid_Main.FlashBid_Main.Auth.domain.UserRole;
import com.FlashBid_Main.FlashBid_Main.Auth.repository.UserRepository;
import com.FlashBid_Main.FlashBid_Main.Auth.util.LocationExtractor.LocationInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@TestPropertySource(properties = {
    "jwt.refresh-expiration=2000"
})
class JwtTokenProviderTest {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private LocationInfo testLocation;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
            .userId("test@example.com")
            .password("password123")
            .nickname("testuser")
            .role(UserRole.USER)
            .build();
        testUser = userRepository.save(testUser);

        testLocation = new LocationInfo("127.0.0.1", "South Korea", "Seoul");
    }

    @AfterEach
    void tearDown() {
        refreshTokenService.deleteByUserId(testUser.getUserId());
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("AccessToken 생성 및 검증 성공")
    void createAccessToken_Success() {
        List<String> roles = List.of("USER");

        String token = jwtTokenProvider.createAccessToken(testUser.getUserId(), testUser.getNickname(), roles);

        assertThat(token).isNotNull();
        assertThat(jwtTokenProvider.validAccessToken(token)).isTrue();
        assertThat(jwtTokenProvider.getUserId(token)).isEqualTo(testUser.getUserId());
        assertThat(jwtTokenProvider.getNickname(token)).isEqualTo(testUser.getNickname());
        assertThat(jwtTokenProvider.getRoles(token)).contains("USER");
        assertThat(jwtTokenProvider.getTokenType(token)).isEqualTo(TokenType.ACCESS);
    }

    @Test
    @DisplayName("RefreshToken 생성 및 Redis 저장 확인 (위치 정보 포함)")
    void createRefreshToken_SavedInRedis_WithLocation() {
        String token = jwtTokenProvider.createRefreshToken(testUser.getUserId(), testLocation);

        assertThat(token).isNotNull();
        assertThat(jwtTokenProvider.validRefreshToken(token)).isTrue();

        Optional<RefreshToken> stored = refreshTokenService.findByUserId(testUser.getUserId());
        assertThat(stored).isPresent();
        assertThat(stored.get().getToken()).isEqualTo(token);
        assertThat(stored.get().getIpAddress()).isEqualTo("127.0.0.1");
        assertThat(stored.get().getCountry()).isEqualTo("South Korea");
        assertThat(stored.get().getCity()).isEqualTo("Seoul");
    }

    @Test
    @DisplayName("RefreshToken으로 AccessToken 갱신 성공 (UserRepository 사용)")
    void renewAccessToken_Success() {
        String refreshToken = jwtTokenProvider.createRefreshToken(testUser.getUserId(), testLocation);

        String newAccessToken = jwtTokenProvider.renewAccessToken(refreshToken);

        assertThat(newAccessToken).isNotNull();
        assertThat(jwtTokenProvider.validAccessToken(newAccessToken)).isTrue();
        assertThat(jwtTokenProvider.getUserId(newAccessToken)).isEqualTo(testUser.getUserId());
        assertThat(jwtTokenProvider.getNickname(newAccessToken)).isEqualTo(testUser.getNickname());
        assertThat(jwtTokenProvider.getRoles(newAccessToken)).contains("USER");
    }

    @Test
    @DisplayName("AccessToken을 RefreshToken으로 검증 시 실패")
    void validateAccessTokenAsRefresh_Fail() {
        String accessToken = jwtTokenProvider.createAccessToken(testUser.getUserId(), testUser.getNickname(), List.of("USER"));

        assertThat(jwtTokenProvider.validRefreshToken(accessToken)).isFalse();
    }

    @Test
    @DisplayName("RefreshToken 무효화 후 재사용 불가")
    void revokeRefreshToken_CannotReuse() {
        String refreshToken = jwtTokenProvider.createRefreshToken(testUser.getUserId(), testLocation);

        jwtTokenProvider.revokeRefreshToken(testUser.getUserId());

        assertThat(jwtTokenProvider.validRefreshToken(refreshToken)).isFalse();
    }

    @Test
    @DisplayName("만료된 RefreshToken으로 갱신 시도 시 예외 발생")
    void renewWithExpiredToken_ThrowsException() {
        String fakeToken = "expired.refresh.token";

        assertThatThrownBy(() -> jwtTokenProvider.renewAccessToken(fakeToken))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid or expired");
    }

    @Test
    @DisplayName("RefreshToken Redis TTL 만료 시 검증 실패")
    void validRefreshToken_ReturnsFalse_WhenRedisTtlExpires() {
        String refreshToken = jwtTokenProvider.createRefreshToken(testUser.getUserId(), testLocation);
        assertThat(jwtTokenProvider.validRefreshToken(refreshToken)).isTrue();

        await()
            .atMost(5, TimeUnit.SECONDS)
            .pollInterval(500, TimeUnit.MILLISECONDS)
            .untilAsserted(() -> {
                assertThat(jwtTokenProvider.validRefreshToken(refreshToken)).isFalse();
            });
    }
}
