package com.FlashBid_Main.FlashBid_Main.Auth.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import java.time.LocalDateTime;

@RedisHash(value = "refreshToken")
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class RefreshToken {

    @Id
    private String userId;

    @Indexed
    private String token;

    private String ipAddress;
    private String country;
    private String city;

    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
}
