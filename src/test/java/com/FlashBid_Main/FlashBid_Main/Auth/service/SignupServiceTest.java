package com.FlashBid_Main.FlashBid_Main.Auth.service;

import com.FlashBid_Main.FlashBid_Main.Auth.domain.User;
import com.FlashBid_Main.FlashBid_Main.Auth.domain.UserRole;
import com.FlashBid_Main.FlashBid_Main.Auth.dto.AvailabilityCheckResponse;
import com.FlashBid_Main.FlashBid_Main.Auth.dto.SignupRequest;
import com.FlashBid_Main.FlashBid_Main.Auth.dto.SignupResponse;
import com.FlashBid_Main.FlashBid_Main.Auth.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class SignupServiceTest {

    @Autowired
    private SignupService signupService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String USERID_LOCK_PREFIX = "signup:lock:userId:";
    private static final String NICKNAME_LOCK_PREFIX = "signup:lock:nickname:";

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
        redisTemplate.delete(redisTemplate.keys(USERID_LOCK_PREFIX + "*"));
        redisTemplate.delete(redisTemplate.keys(NICKNAME_LOCK_PREFIX + "*"));
    }

    @Nested
    @DisplayName("checkUserIdAvailability 테스트")
    class CheckUserIdAvailabilityTest {

        @Test
        @DisplayName("null 입력 시 unavailable 반환")
        void checkUserIdAvailability_NullInput_ReturnsUnavailable() {
            AvailabilityCheckResponse response = signupService.checkUserIdAvailability(null);

            assertThat(response.isAvailable()).isFalse();
            assertThat(response.getMessage()).isEqualTo("아이디를 입력해주세요.");
        }

        @Test
        @DisplayName("빈 문자열 입력 시 unavailable 반환")
        void checkUserIdAvailability_BlankInput_ReturnsUnavailable() {
            AvailabilityCheckResponse response = signupService.checkUserIdAvailability("   ");

            assertThat(response.isAvailable()).isFalse();
            assertThat(response.getMessage()).isEqualTo("아이디를 입력해주세요.");
        }

        @Test
        @DisplayName("이미 존재하는 아이디 입력 시 unavailable 반환")
        void checkUserIdAvailability_ExistingUserId_ReturnsUnavailable() {
            User existingUser = User.builder()
                .userId("existingUser")
                .password("password123")
                .nickname("nickname")
                .role(UserRole.USER)
                .build();
            userRepository.save(existingUser);

            AvailabilityCheckResponse response = signupService.checkUserIdAvailability("existingUser");

            assertThat(response.isAvailable()).isFalse();
            assertThat(response.getMessage()).isEqualTo("이미 사용 중인 아이디입니다.");
        }

        @Test
        @DisplayName("Redis Lock이 걸린 아이디 입력 시 unavailable 반환")
        void checkUserIdAvailability_LockedUserId_ReturnsUnavailable() {
            String userId = "lockedUserId";
            redisTemplate.opsForValue().set(USERID_LOCK_PREFIX + userId, "locked");

            AvailabilityCheckResponse response = signupService.checkUserIdAvailability(userId);

            assertThat(response.isAvailable()).isFalse();
            assertThat(response.getMessage()).isEqualTo("다른 사용자가 선점 중인 아이디입니다.");
        }

        @Test
        @DisplayName("사용 가능한 아이디 입력 시 available 반환 및 Redis Lock 설정")
        void checkUserIdAvailability_AvailableUserId_ReturnsAvailableAndSetsLock() {
            String userId = "newUserId";

            AvailabilityCheckResponse response = signupService.checkUserIdAvailability(userId);

            assertThat(response.isAvailable()).isTrue();
            assertThat(response.getMessage()).isEqualTo("사용 가능한 아이디입니다.");
            assertThat(redisTemplate.hasKey(USERID_LOCK_PREFIX + userId)).isTrue();
        }
    }

    @Nested
    @DisplayName("checkNicknameAvailability 테스트")
    class CheckNicknameAvailabilityTest {

        @Test
        @DisplayName("null 입력 시 unavailable 반환")
        void checkNicknameAvailability_NullInput_ReturnsUnavailable() {
            AvailabilityCheckResponse response = signupService.checkNicknameAvailability(null);

            assertThat(response.isAvailable()).isFalse();
            assertThat(response.getMessage()).isEqualTo("닉네임을 입력해주세요.");
        }

        @Test
        @DisplayName("빈 문자열 입력 시 unavailable 반환")
        void checkNicknameAvailability_BlankInput_ReturnsUnavailable() {
            AvailabilityCheckResponse response = signupService.checkNicknameAvailability("   ");

            assertThat(response.isAvailable()).isFalse();
            assertThat(response.getMessage()).isEqualTo("닉네임을 입력해주세요.");
        }

        @Test
        @DisplayName("이미 존재하는 닉네임 입력 시 unavailable 반환")
        void checkNicknameAvailability_ExistingNickname_ReturnsUnavailable() {
            User existingUser = User.builder()
                .userId("someUser")
                .password("password123")
                .nickname("existingNickname")
                .role(UserRole.USER)
                .build();
            userRepository.save(existingUser);

            AvailabilityCheckResponse response = signupService.checkNicknameAvailability("existingNickname");

            assertThat(response.isAvailable()).isFalse();
            assertThat(response.getMessage()).isEqualTo("이미 사용 중인 닉네임입니다.");
        }

        @Test
        @DisplayName("Redis Lock이 걸린 닉네임 입력 시 unavailable 반환")
        void checkNicknameAvailability_LockedNickname_ReturnsUnavailable() {
            String nickname = "lockedNickname";
            redisTemplate.opsForValue().set(NICKNAME_LOCK_PREFIX + nickname, "locked");

            AvailabilityCheckResponse response = signupService.checkNicknameAvailability(nickname);

            assertThat(response.isAvailable()).isFalse();
            assertThat(response.getMessage()).isEqualTo("다른 사용자가 선점 중인 닉네임입니다.");
        }

        @Test
        @DisplayName("사용 가능한 닉네임 입력 시 available 반환 및 Redis Lock 설정")
        void checkNicknameAvailability_AvailableNickname_ReturnsAvailableAndSetsLock() {
            String nickname = "newNickname";

            AvailabilityCheckResponse response = signupService.checkNicknameAvailability(nickname);

            assertThat(response.isAvailable()).isTrue();
            assertThat(response.getMessage()).isEqualTo("사용 가능한 닉네임입니다.");
            assertThat(redisTemplate.hasKey(NICKNAME_LOCK_PREFIX + nickname)).isTrue();
        }
    }

    @Nested
    @DisplayName("registerUser 테스트")
    class RegisterUserTest {

        @Test
        @DisplayName("비밀번호 불일치 시 failure 반환")
        void registerUser_PasswordMismatch_ReturnsFailure() {
            SignupRequest request = new SignupRequest(
                "nickname",
                "userId",
                "password123",
                "differentPassword"
            );

            SignupResponse response = signupService.registerUser(request);

            assertThat(response.isSuccess()).isFalse();
            assertThat(response.getMessage()).isEqualTo("비밀번호가 일치하지 않습니다.");
            assertThat(response.getRedirectUrl()).isNull();
        }

        @Test
        @DisplayName("중복 아이디로 가입 시도 시 failure 반환")
        void registerUser_DuplicateUserId_ReturnsFailure() {
            User existingUser = User.builder()
                .userId("duplicateId")
                .password("password123")
                .nickname("existingNick")
                .role(UserRole.USER)
                .build();
            userRepository.save(existingUser);

            SignupRequest request = new SignupRequest(
                "newNickname",
                "duplicateId",
                "password123",
                "password123"
            );

            SignupResponse response = signupService.registerUser(request);

            assertThat(response.isSuccess()).isFalse();
            assertThat(response.getMessage()).isEqualTo("이미 사용 중인 아이디입니다.");
        }

        @Test
        @DisplayName("중복 닉네임으로 가입 시도 시 failure 반환")
        void registerUser_DuplicateNickname_ReturnsFailure() {
            User existingUser = User.builder()
                .userId("existingId")
                .password("password123")
                .nickname("duplicateNick")
                .role(UserRole.USER)
                .build();
            userRepository.save(existingUser);

            SignupRequest request = new SignupRequest(
                "duplicateNick",
                "newUserId",
                "password123",
                "password123"
            );

            SignupResponse response = signupService.registerUser(request);

            assertThat(response.isSuccess()).isFalse();
            assertThat(response.getMessage()).isEqualTo("이미 사용 중인 닉네임입니다.");
        }

        @Test
        @DisplayName("회원가입 성공 시 User 생성 및 Lock 해제")
        void registerUser_Success_CreatesUserAndReleasesLocks() {
            String userId = "newUserId";
            String nickname = "newNickname";

            redisTemplate.opsForValue().set(USERID_LOCK_PREFIX + userId, "locked");
            redisTemplate.opsForValue().set(NICKNAME_LOCK_PREFIX + nickname, "locked");

            SignupRequest request = new SignupRequest(
                nickname,
                userId,
                "password123",
                "password123"
            );

            SignupResponse response = signupService.registerUser(request);

            assertThat(response.isSuccess()).isTrue();
            assertThat(response.getMessage()).isEqualTo("회원가입이 완료되었습니다.");
            assertThat(response.getRedirectUrl()).isEqualTo("/login");

            assertThat(userRepository.existsByUserId(userId)).isTrue();
            assertThat(userRepository.existsByNickname(nickname)).isTrue();

            assertThat(redisTemplate.hasKey(USERID_LOCK_PREFIX + userId)).isFalse();
            assertThat(redisTemplate.hasKey(NICKNAME_LOCK_PREFIX + nickname)).isFalse();
        }

        @Test
        @DisplayName("회원가입 성공 시 비밀번호 암호화 확인")
        void registerUser_Success_PasswordIsEncoded() {
            SignupRequest request = new SignupRequest(
                "nickname",
                "userId",
                "password123",
                "password123"
            );

            signupService.registerUser(request);

            User savedUser = userRepository.findByUserId("userId").orElseThrow();
            assertThat(savedUser.getPassword()).isNotEqualTo("password123");
            assertThat(savedUser.getPassword()).startsWith("$2a$");
        }
    }
}
