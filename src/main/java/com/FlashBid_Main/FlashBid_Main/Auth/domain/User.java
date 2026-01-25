package com.FlashBid_Main.FlashBid_Main.Auth.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 50)
  private String userId;

  @Column(nullable = false)
  private String password;

  @Column(nullable = false, unique = true, length = 30)
  private String nickname;

  @Column(nullable = false)
  private Long point = 100000L;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private UserRole role = UserRole.USER;

  @CreatedDate
  @Column(updatable = false)
  private LocalDateTime createdAt;

  @LastModifiedDate
  private LocalDateTime updatedAt;

  @Builder
  public User(String userId, String password, String nickname, Long point, UserRole role) {
    this.userId = userId;
    this.password = password;
    this.nickname = nickname;
    this.point = (point != null) ? point : 100000L;
    this.role = (role != null) ? role : UserRole.USER;
  }

  public void deductPoint(Long amount) {
    if (this.point < amount) {
      throw new IllegalArgumentException("잔액이 부족합니다.");
    }
    this.point -= amount;
  }

  public void addPoint(Long amount) {
    this.point += amount;
  }
}