package com.FlashBid_Main.FlashBid_Main.Auth.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
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

    @TimeToLive
    private Long ttl;

    private String ipAddress;
    private String country;
    private String city;

    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;


    public RefreshToken(String userId, String token, String ipAddress, String country, String city, Long ttl){
        this.userId = userId;
        this.token = token;
        this.ipAddress = ipAddress;
        this.country = country;
        this.city = city;
        this.ttl = ttl;
        this.createdAt = LocalDateTime.now();
        this.expiresAt = LocalDateTime.now().plusSeconds(ttl);
    }
}
