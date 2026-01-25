package com.FlashBid_Main.FlashBid_Main.Auth.service;


import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class JwtTokenProviderTest {

  private JwtTokenProvider jwtTokenProvider;

  private final String testSecretKey = "test-secret-key-for-flashbid-project-at-least-32-bytes-long";
  private final long testValidity = 3600000;

  @BeforeEach
  void setUp() {
    jwtTokenProvider = new JwtTokenProvider(testSecretKey);

    ReflectionTestUtils.setField(jwtTokenProvider, "validity", testValidity);
  }

  @Test
  @DisplayName("토큰 생성 및 유효성 검증 성공 테스트")
  void createAndValidateToken_Success() {
    String username = "user@example.com";
    List<String> roles = List.of("ROLE_USER");

    String token = jwtTokenProvider.createToken(username, roles);
    boolean isValid = jwtTokenProvider.validToken(token);

    assertThat(token).isNotNull();
    assertThat(isValid).isTrue();
  }

  @Test
  @DisplayName("토큰에서 정확한 Username(Subject)을 추출하는지 테스트")
  void getUsername_Success() {
    String expectedUsername = "flashbid_admin";
    String token = jwtTokenProvider.createToken(expectedUsername, List.of("ROLE_ADMIN"));

    String extractedUsername = jwtTokenProvider.getUsername(token);

    assertThat(extractedUsername).isEqualTo(expectedUsername);
  }

  @Test
  @DisplayName("변조된 토큰으로 검증 시 실패해야 함")
  void validateToken_InvalidSignature_Fail() {
    String token = jwtTokenProvider.createToken("user", List.of("USER"));
    String tamperedToken = token + "modified";

    boolean isValid = jwtTokenProvider.validToken(tamperedToken);

    assertThat(isValid).isFalse();
  }

  @Test
  @DisplayName("만료된 토큰 검증 시 실패해야 함")
  void validateToken_Expired_Fail() {
    ReflectionTestUtils.setField(jwtTokenProvider, "validity", 0L);
    String token = jwtTokenProvider.createToken("user", List.of("USER"));

    boolean isValid = jwtTokenProvider.validToken(token);

    assertThat(isValid).isFalse();
  }
}