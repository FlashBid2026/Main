package com.FlashBid_Main.FlashBid_Main.Item.dto;

import com.FlashBid_Main.FlashBid_Main.Item.domain.Item;
import com.FlashBid_Main.FlashBid_Main.Item.domain.ItemImage;
import lombok.Builder;
import lombok.Getter;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
public class ItemDetailResponse {

  private static final String GCS_BASE_URL = "https://storage.googleapis.com/junyeong_buket/";
  private static final String DEFAULT_IMAGE_URL = "/images/default-auction.png";

  private Long id;
  private String title;
  private String description;
  private String category;
  private Long startPrice;
  private Long currentPrice;
  private List<String> imageUrls;
  private String endTime;
  private String remainingTime;
  private String sellerNickname;
  private String currentWinnerNickname;

  public static ItemDetailResponse from(Item item) {
    return ItemDetailResponse.builder()
        .id(item.getId())
        .title(item.getItemName())
        .description(item.getDescription())
        .category(item.getCategory().getDisplayName())
        .startPrice(item.getStartPrice())
        .currentPrice(item.getCurrentPrice())
        .imageUrls(extractImageUrls(item))
        .endTime(item.getEndTime().toString())
        .remainingTime(calculateRemainingTime(item.getEndTime()))
        .sellerNickname(item.getSeller() != null ? item.getSeller().getNickname() : "알 수 없음")
        .currentWinnerNickname(item.getCurrentWinner() != null ? item.getCurrentWinner().getNickname() : null)
        .build();
  }

  private static List<String> extractImageUrls(Item item) {
    if (item.getImages() == null || item.getImages().isEmpty()) {
      return List.of(DEFAULT_IMAGE_URL);
    }
    List<String> urls = new ArrayList<>();
    for (ItemImage image : item.getImages()) {
      if (image.getGcsFileName() != null && !image.getGcsFileName().isEmpty()) {
        urls.add(GCS_BASE_URL + image.getGcsFileName());
      }
    }
    if (urls.isEmpty()) {
      return List.of(DEFAULT_IMAGE_URL);
    }
    return urls;
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
