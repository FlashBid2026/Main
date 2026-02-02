package com.FlashBid_Main.FlashBid_Main.Item.dto;

import com.FlashBid_Main.FlashBid_Main.Item.domain.Item;
import com.FlashBid_Main.FlashBid_Main.Item.domain.ItemImage;
import lombok.Builder;
import lombok.Getter;

import java.time.Duration;
import java.time.LocalDateTime;

@Getter
@Builder
public class ItemListResponse {

  private static final String GCS_BASE_URL = "https://storage.googleapis.com/junyeong_buket/";
  private static final String DEFAULT_IMAGE_URL = "/images/default-auction.png";

  private Long id;
  private String title;
  private String imageUrl;
  private Long currentPrice;
  private String remainingTime;
  private String endTime;
  private String category;

  public static ItemListResponse from(Item item) {
    return ItemListResponse.builder()
        .id(item.getId())
        .title(item.getItemName())
        .imageUrl(extractImageUrl(item))
        .currentPrice(item.getCurrentPrice())
        .remainingTime(calculateRemainingTime(item.getEndTime()))
        .endTime(item.getEndTime().toString())
        .category(item.getCategory().getDisplayName())
        .build();
  }

  private static String extractImageUrl(Item item) {
    if (item.getImages() == null || item.getImages().isEmpty()) {
      return DEFAULT_IMAGE_URL;
    }
    ItemImage firstImage = item.getImages().get(0);
    if (firstImage.getGcsFileName() == null || firstImage.getGcsFileName().isEmpty()) {
      return DEFAULT_IMAGE_URL;
    }
    return GCS_BASE_URL + firstImage.getGcsFileName();
  }

  private static String calculateRemainingTime(LocalDateTime endTime) {
    LocalDateTime now = LocalDateTime.now();
    if (endTime.isBefore(now)) {
      return "종료됨";
    }

    Duration duration = Duration.between(now, endTime);
    long days = duration.toDays();

    if (days >= 1) {
      return days + "일 남음";
    }

    long hours = duration.toHours();
    long minutes = duration.toMinutesPart();
    long seconds = duration.toSecondsPart();

    return String.format("%02d:%02d:%02d", hours, minutes, seconds);
  }
}
