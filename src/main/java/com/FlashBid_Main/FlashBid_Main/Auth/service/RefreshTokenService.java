package com.FlashBid_Main.FlashBid_Main.Auth.service;

import com.FlashBid_Main.FlashBid_Main.Auth.domain.RefreshToken;
import com.FlashBid_Main.FlashBid_Main.Auth.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    public void save(RefreshToken refreshToken) {
        refreshTokenRepository.save(refreshToken);
    }

    public Optional<RefreshToken> findByUserId(String userId) {
        return refreshTokenRepository.findById(userId);
    }

    public Optional<RefreshToken> findByToken(String tokenValue) {
        return refreshTokenRepository.findByToken(tokenValue);
    }

    public void deleteByUserId(String userId) {
        refreshTokenRepository.deleteById(userId);
    }

}
