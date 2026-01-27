package com.FlashBid_Main.FlashBid_Main.Auth.repository;

import com.FlashBid_Main.FlashBid_Main.Auth.domain.RefreshToken;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends CrudRepository<RefreshToken, String> {
    Optional<RefreshToken> findByToken(String token);
    void deleteByUserId(String userId);
}
