package com.FlashBid_Main.FlashBid_Main.Item.service;

import com.FlashBid_Main.FlashBid_Main.Auth.domain.User;
import com.FlashBid_Main.FlashBid_Main.Gcs.service.GcsService;
import com.FlashBid_Main.FlashBid_Main.Item.domain.Item;
import com.FlashBid_Main.FlashBid_Main.Item.domain.ItemImage;
import com.FlashBid_Main.FlashBid_Main.Item.dto.ItemRegistrationDto;
import com.FlashBid_Main.FlashBid_Main.Item.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemService {

  private final ItemRepository itemRepository;
  private final GcsService gcsService;

  @Transactional
  public Long registerItem(ItemRegistrationDto dto, User seller) throws IOException {

    LocalDateTime calculatedEndTime = LocalDateTime.now().plusHours(dto.getDurationHour());

    Item item = Item.builder()
        .itemName(dto.getItemName())
        .description(dto.getDescription())
        .category(dto.getCategory())
        .startPrice(dto.getStartPrice())
        .endTime(calculatedEndTime)
        .seller(seller)
        .build();

    if (dto.getImages() != null) {
      for (MultipartFile file : dto.getImages()) {
        if (!file.isEmpty()) {
          String gcsFileName = gcsService.uploadImage(file);
          item.addImage(new ItemImage(gcsFileName));
        }
      }
    }

    return itemRepository.save(item).getId();
  }
}
