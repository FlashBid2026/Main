package com.FlashBid_Main.FlashBid_Main.Auth.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

class CustomUserDetailsTest {

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

    @Nested
    @DisplayName("from() 정적 팩토리 메서드 테스트")
    class FromMethodTest {

        @Test
        @DisplayName("User 엔티티로부터 CustomUserDetails를 올바르게 생성한다")
        void createCustomUserDetails_FromUser_Success() {
            CustomUserDetails userDetails = CustomUserDetails.from(testUser);

            assertThat(userDetails).isNotNull();
            assertThat(userDetails.getUserId()).isEqualTo("testuser@example.com");
            assertThat(userDetails.getPassword()).isEqualTo("encodedPassword123");
            assertThat(userDetails.getNickname()).isEqualTo("테스트유저");
        }

        @Test
        @DisplayName("USER 역할을 가진 사용자의 권한이 ROLE_USER로 설정된다")
        void createCustomUserDetails_WithUserRole_HasRoleUserAuthority() {
            CustomUserDetails userDetails = CustomUserDetails.from(testUser);

            Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();

            assertThat(authorities).hasSize(1);
            assertThat(authorities)
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_USER");
        }

        @Test
        @DisplayName("ADMIN 역할을 가진 사용자의 권한이 ROLE_ADMIN으로 설정된다")
        void createCustomUserDetails_WithAdminRole_HasRoleAdminAuthority() {
            User adminUser = User.builder()
                .userId("admin@example.com")
                .password("adminPassword")
                .nickname("관리자")
                .role(UserRole.ADMIN)
                .build();

            CustomUserDetails userDetails = CustomUserDetails.from(adminUser);

            Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();

            assertThat(authorities).hasSize(1);
            assertThat(authorities)
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_ADMIN");
        }
    }

    @Nested
    @DisplayName("UserDetails 인터페이스 구현 테스트")
    class UserDetailsInterfaceTest {

        @Test
        @DisplayName("getUsername()은 userId를 반환한다")
        void getUsername_ReturnsUserId() {
            CustomUserDetails userDetails = CustomUserDetails.from(testUser);

            assertThat(userDetails.getUsername()).isEqualTo("testuser@example.com");
        }

        @Test
        @DisplayName("getPassword()는 암호화된 비밀번호를 반환한다")
        void getPassword_ReturnsEncodedPassword() {
            CustomUserDetails userDetails = CustomUserDetails.from(testUser);

            assertThat(userDetails.getPassword()).isEqualTo("encodedPassword123");
        }

        @Test
        @DisplayName("isAccountNonExpired()는 true를 반환한다")
        void isAccountNonExpired_ReturnsTrue() {
            CustomUserDetails userDetails = CustomUserDetails.from(testUser);

            assertThat(userDetails.isAccountNonExpired()).isTrue();
        }

        @Test
        @DisplayName("isAccountNonLocked()는 true를 반환한다")
        void isAccountNonLocked_ReturnsTrue() {
            CustomUserDetails userDetails = CustomUserDetails.from(testUser);

            assertThat(userDetails.isAccountNonLocked()).isTrue();
        }

        @Test
        @DisplayName("isCredentialsNonExpired()는 true를 반환한다")
        void isCredentialsNonExpired_ReturnsTrue() {
            CustomUserDetails userDetails = CustomUserDetails.from(testUser);

            assertThat(userDetails.isCredentialsNonExpired()).isTrue();
        }

        @Test
        @DisplayName("isEnabled()는 true를 반환한다")
        void isEnabled_ReturnsTrue() {
            CustomUserDetails userDetails = CustomUserDetails.from(testUser);

            assertThat(userDetails.isEnabled()).isTrue();
        }
    }
}
