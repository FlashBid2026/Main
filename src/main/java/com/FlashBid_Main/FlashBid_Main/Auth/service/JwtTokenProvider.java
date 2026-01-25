package com.FlashBid_Main.FlashBid_Main.Auth.service;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@Component
public class JwtTokenProvider {

  @Value("${jwt.secret.expiration}")
  private long validity;

  private SecretKey secretKey;

  public JwtTokenProvider(@Value("${jwt.secret}") String key){
    this.secretKey = Keys.hmacShaKeyFor(key.getBytes(StandardCharsets.UTF_8));
  }

  public String createToken(String username, List<String> roles){
    Date now = new Date();
    Date expireDate = new Date(now.getTime() + validity);


    return Jwts.builder()
        .header().add("typ", "JWT").and()
        .subject(username)
        .claim("role", roles)
        .issuedAt(now)
        .expiration(expireDate)
        .signWith(secretKey)
        .compact();
  }

  public boolean validToken(String token){
    try{
      Jwts.parser()
          .verifyWith(secretKey)
          .build()
          .parseSignedClaims(token);

      return true;
    }catch (JwtException | IllegalArgumentException e){
      return false;
    }
  }


  public String getUsername(String token){
    return Jwts.parser()
        .verifyWith(secretKey)
        .build()
        .parseSignedClaims(token)
        .getPayload()
        .getSubject();
  }
}
