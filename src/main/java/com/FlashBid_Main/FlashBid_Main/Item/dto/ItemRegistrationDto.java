package com.FlashBid_Main.FlashBid_Main.Item.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
public class ItemRegistrationDto {
  private String itemName;
  private String description;
  private Long startPrice;
  private Integer durationHour;
  private List<MultipartFile> images;
}