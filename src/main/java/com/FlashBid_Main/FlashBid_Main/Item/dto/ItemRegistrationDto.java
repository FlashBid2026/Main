package com.FlashBid_Main.FlashBid_Main.Item.dto;

import com.FlashBid_Main.FlashBid_Main.Item.domain.Category;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
public class ItemRegistrationDto {
  private Category category;
  private String itemName;
  private String description;
  private Long startPrice;
  private Integer durationHour;
  private List<MultipartFile> images;
}