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
  private Long availablePoint = 100000L;

  @Column(nullable = false)
  private Long lockedPoint = 0L;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private UserRole role = UserRole.USER;

  @CreatedDate
  @Column(updatable = false)
  private LocalDateTime createdAt;

  @LastModifiedDate
  private LocalDateTime updatedAt;

  @Builder
  public User(String userId, String password, String nickname, Long availablePoint, UserRole role) {
    this.userId = userId;
    this.password = password;
    this.nickname = nickname;
    this.availablePoint = (availablePoint != null) ? availablePoint : 100000L;
    this.lockedPoint = 0L;
    this.role = (role != null) ? role : UserRole.USER;
  }

  public void lockPoint(Long amount) {
    if (this.availablePoint < amount) {
      throw new IllegalArgumentException("사용 가능한 잔액이 부족합니다.");
    }
    this.availablePoint -= amount;
    this.lockedPoint += amount;
  }

  public void unlockPoint(Long amount) {
    if (this.lockedPoint < amount) {
      throw new IllegalStateException("복구할 잠금 포인트가 부족합니다.");
    }
    this.lockedPoint -= amount;
    this.availablePoint += amount;
  }

  public void confirmPayment(Long amount) {
    if (this.lockedPoint < amount) {
      throw new IllegalStateException("결제할 잠금 포인트가 부족합니다.");
    }
    this.lockedPoint -= amount;
  }
}