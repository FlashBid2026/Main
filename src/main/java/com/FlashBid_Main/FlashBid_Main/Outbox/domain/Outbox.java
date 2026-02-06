package com.FlashBid_Main.FlashBid_Main.Outbox.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "outbox", indexes = {
    @Index(name = "idx_outbox_processed_created_at", columnList = "processed, createdAt")
})
public class Outbox {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long aggregateId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private EventType eventType;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String payload;

  @Column(nullable = false)
  private boolean processed = false;

  @Column(nullable = false)
  private LocalDateTime createdAt;

  @Builder
  public Outbox(Long aggregateId, EventType eventType, String payload) {
    this.aggregateId = aggregateId;
    this.eventType = eventType;
    this.payload = payload;
    this.createdAt = LocalDateTime.now();
    this.processed = false;
  }

  public void complete() {
    this.processed = true;
  }
}