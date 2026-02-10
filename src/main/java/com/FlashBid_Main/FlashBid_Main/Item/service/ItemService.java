package com.FlashBid_Main.FlashBid_Main.Item.service;

import com.FlashBid_Main.FlashBid_Main.Auth.domain.User;
import com.FlashBid_Main.FlashBid_Main.Auth.repository.UserRepository;
import com.FlashBid_Main.FlashBid_Main.Gcs.service.GcsService;
import com.FlashBid_Main.FlashBid_Main.Item.domain.Item;
import com.FlashBid_Main.FlashBid_Main.Item.domain.ItemImage;
import com.FlashBid_Main.FlashBid_Main.Item.dto.ItemDetailResponse;
import com.FlashBid_Main.FlashBid_Main.Item.dto.ItemListResponse;
import com.FlashBid_Main.FlashBid_Main.Item.dto.ItemRegistrationDto;
import com.FlashBid_Main.FlashBid_Main.Item.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemService {

  private final ItemRepository itemRepository;
  private final UserRepository userRepository;
  private final GcsService gcsService;

  @Transactional
  public Long registerItem(ItemRegistrationDto dto, String userId) throws IOException {
    User seller = userRepository.findByUserId(userId)
        .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

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

  public ItemDetailResponse getItemDetail(Long itemId) {
    Item item = itemRepository.findById(itemId)
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 경매 상품입니다."));
    return ItemDetailResponse.from(item);
  }

  public List<ItemListResponse> getActiveItemsForHome() {
    return itemRepository.findByEndTimeAfterOrderByEndTimeAsc(
            LocalDateTime.now(),
            PageRequest.of(0, 12)
        )
        .map(ItemListResponse::from)
        .getContent();
  }
}
