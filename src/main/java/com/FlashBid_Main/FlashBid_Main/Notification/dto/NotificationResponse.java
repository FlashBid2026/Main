package com.FlashBid_Main.FlashBid_Main.Notification.dto;

import com.FlashBid_Main.FlashBid_Main.Notification.domain.Notification;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public record NotificationResponse(
    Long id,
    String type,
    String title,
    String message,
    String targetUrl,
    boolean read,
    LocalDateTime createdAt
) {
    public static NotificationResponse from(Notification n) {
        return new NotificationResponse(
            n.getId(),
            n.getType(),
            n.getTitle(),
            n.getMessage(),
            n.getTargetUrl(),
            n.isRead(),
            n.getCreatedAt()
        );
    }
}
