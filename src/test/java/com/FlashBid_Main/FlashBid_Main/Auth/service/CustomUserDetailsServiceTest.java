package com.FlashBid_Main.FlashBid_Main.Auth.service;

import com.FlashBid_Main.FlashBid_Main.Auth.domain.CustomUserDetails;
import com.FlashBid_Main.FlashBid_Main.Auth.domain.User;
import com.FlashBid_Main.FlashBid_Main.Auth.domain.UserRole;
import com.FlashBid_Main.FlashBid_Main.Auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
            .userId("testuser@example.com")
            .password("encodedPassword123")
            .nickname("테스트유저")
            .role(UserRole.USER)
            .build();
    }

    @Test
    @DisplayName("존재하는 사용자 ID로 조회 시 CustomUserDetails를 반환한다")
    void loadUserByUsername_WithExistingUser_ReturnsCustomUserDetails() {
        String userId = "testuser@example.com";
        given(userRepository.findByUserId(userId)).willReturn(Optional.of(testUser));

        UserDetails result = customUserDetailsService.loadUserByUsername(userId);

        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(CustomUserDetails.class);
        assertThat(result.getUsername()).isEqualTo(userId);
        assertThat(result.getPassword()).isEqualTo("encodedPassword123");
        verify(userRepository).findByUserId(userId);
    }

    @Test
    @DisplayName("존재하지 않는 사용자 ID로 조회 시 UsernameNotFoundException이 발생한다")
    void loadUserByUsername_WithNonExistingUser_ThrowsUsernameNotFoundException() {
        String nonExistingUserId = "nonexistent@example.com";
        given(userRepository.findByUserId(nonExistingUserId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername(nonExistingUserId))
            .isInstanceOf(UsernameNotFoundException.class)
            .hasMessageContaining("사용자를 찾을 수 없습니다")
            .hasMessageContaining(nonExistingUserId);

        verify(userRepository).findByUserId(nonExistingUserId);
    }

    @Test
    @DisplayName("조회된 사용자의 권한이 올바르게 설정된다")
    void loadUserByUsername_ReturnsUserDetailsWithCorrectAuthorities() {
        String userId = "testuser@example.com";
        given(userRepository.findByUserId(userId)).willReturn(Optional.of(testUser));

        UserDetails result = customUserDetailsService.loadUserByUsername(userId);

        assertThat(result.getAuthorities())
            .extracting("authority")
            .containsExactly("ROLE_USER");
    }

    @Test
    @DisplayName("ADMIN 역할의 사용자 조회 시 ROLE_ADMIN 권한을 가진 UserDetails를 반환한다")
    void loadUserByUsername_WithAdminUser_ReturnsUserDetailsWithAdminAuthority() {
        User adminUser = User.builder()
            .userId("admin@example.com")
            .password("adminPassword")
            .nickname("관리자")
            .role(UserRole.ADMIN)
            .build();

        given(userRepository.findByUserId("admin@example.com")).willReturn(Optional.of(adminUser));

        UserDetails result = customUserDetailsService.loadUserByUsername("admin@example.com");

        assertThat(result.getAuthorities())
            .extracting("authority")
            .containsExactly("ROLE_ADMIN");
    }
}
