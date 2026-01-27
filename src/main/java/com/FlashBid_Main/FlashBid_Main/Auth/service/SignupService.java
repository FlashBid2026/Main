package com.FlashBid_Main.FlashBid_Main.Auth.service;

import com.FlashBid_Main.FlashBid_Main.Auth.domain.User;
import com.FlashBid_Main.FlashBid_Main.Auth.dto.AvailabilityCheckResponse;
import com.FlashBid_Main.FlashBid_Main.Auth.dto.SignupRequest;
import com.FlashBid_Main.FlashBid_Main.Auth.dto.SignupResponse;
import com.FlashBid_Main.FlashBid_Main.Auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class SignupService {

  private static final String USERID_LOCK_PREFIX = "signup:lock:userId:";
  private static final String NICKNAME_LOCK_PREFIX = "signup:lock:nickname:";
  private static final Duration LOCK_TTL = Duration.ofMinutes(15);

  private final UserRepository userRepository;
  private final StringRedisTemplate redisTemplate;
  private final PasswordEncoder passwordEncoder;

  public AvailabilityCheckResponse checkUserIdAvailability(String userId) {
    if (userId == null || userId.isBlank()) {
      return AvailabilityCheckResponse.unavailable("아이디를 입력해주세요.");
    }

    if (userRepository.existsByUserId(userId)) {
      return AvailabilityCheckResponse.unavailable("이미 사용 중인 아이디입니다.");
    }

    String lockKey = USERID_LOCK_PREFIX + userId;

    Boolean isAcquired = redisTemplate.opsForValue().setIfAbsent(lockKey, "locked", LOCK_TTL);

    if (Boolean.FALSE.equals(isAcquired)) {
      return AvailabilityCheckResponse.unavailable("다른 사용자가 선점 중인 아이디입니다.");
    }

    return AvailabilityCheckResponse.available("사용 가능한 아이디입니다.");
  }

  public AvailabilityCheckResponse checkNicknameAvailability(String nickname) {
    if (nickname == null || nickname.isBlank()) {
      return AvailabilityCheckResponse.unavailable("닉네임을 입력해주세요.");
    }

    if (userRepository.existsByNickname(nickname)) {
      return AvailabilityCheckResponse.unavailable("이미 사용 중인 닉네임입니다.");
    }

    String lockKey = NICKNAME_LOCK_PREFIX + nickname;

    Boolean isAcquired = redisTemplate.opsForValue().setIfAbsent(lockKey, "locked", LOCK_TTL);

    if (Boolean.FALSE.equals(isAcquired)) {
      return AvailabilityCheckResponse.unavailable("다른 사용자가 선점 중인 닉네임입니다.");
    }

    return AvailabilityCheckResponse.available("사용 가능한 닉네임입니다.");
  }

  @Transactional
  public SignupResponse registerUser(SignupRequest request) {
    if (!request.getPassword().equals(request.getConfirmPassword())) {
      return SignupResponse.failure("비밀번호가 일치하지 않습니다.");
    }

    if (userRepository.existsByUserId(request.getUserId())) {
      return SignupResponse.failure("이미 사용 중인 아이디입니다.");
    }

    if (userRepository.existsByNickname(request.getNickname())) {
      return SignupResponse.failure("이미 사용 중인 닉네임입니다.");
    }

    User user = User.builder()
        .userId(request.getUserId())
        .password(passwordEncoder.encode(request.getPassword()))
        .nickname(request.getNickname())
        .build();

    userRepository.save(user);

    releaseLock(USERID_LOCK_PREFIX + request.getUserId());
    releaseLock(NICKNAME_LOCK_PREFIX + request.getNickname());

    return SignupResponse.success("회원가입이 완료되었습니다.", "/login");
  }

  private void releaseLock(String lockKey) {
    redisTemplate.delete(lockKey);
  }
}
