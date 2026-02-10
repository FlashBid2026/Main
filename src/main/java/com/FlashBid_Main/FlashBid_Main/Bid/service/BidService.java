package com.FlashBid_Main.FlashBid_Main.Bid.service;

import com.FlashBid_Main.FlashBid_Main.Auth.domain.User;
import com.FlashBid_Main.FlashBid_Main.Auth.repository.UserRepository;
import com.FlashBid_Main.FlashBid_Main.Bid.domain.Bid;
import com.FlashBid_Main.FlashBid_Main.Bid.dto.BidRequest;
import com.FlashBid_Main.FlashBid_Main.Bid.dto.BidResponse;
import com.FlashBid_Main.FlashBid_Main.Bid.repository.BidRepository;
import com.FlashBid_Main.FlashBid_Main.Item.domain.Item;
import com.FlashBid_Main.FlashBid_Main.Item.repository.ItemRepository;
import com.FlashBid_Main.FlashBid_Main.Outbox.domain.EventType;
import com.FlashBid_Main.FlashBid_Main.Outbox.domain.Outbox;
import com.FlashBid_Main.FlashBid_Main.Outbox.repository.OutboxRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BidService {

  private final ItemRepository itemRepository;
  private final UserRepository userRepository;
  private final BidRepository bidRepository;
  private final OutboxRepository outboxRepository;
  private final ObjectMapper objectMapper;

  @Transactional
  public BidResponse placeBid(BidRequest request, String userId) {
    // 로컬 캐시 및 Redis 필터링 (나중에 추가)

    Item item = itemRepository.findByIdWithPessimisticLock(request.itemId())
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다."));

    // 경매 종료 여부 확인
    if (item.getEndTime().isBefore(LocalDateTime.now())) {
      return new BidResponse(false, "경매가 이미 종료되었습니다.", item.getCurrentPrice(), null);
    }

    // 최고가 비교
    if (request.bidAmount() <= item.getCurrentPrice()) {
      return new BidResponse(false, "현재 최고가보다 높은 금액을 입찰해야 합니다.", item.getCurrentPrice(), null);
    }

    User newWinner = userRepository.findByUserId(userId)
        .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

    // 이전 최고가 유저가 있다면 돈을 Unlock 해줍니다.
    if (item.getCurrentWinner() != null) {
      User previousWinner = item.getCurrentWinner();
      previousWinner.unlockPoint(item.getCurrentPrice());
    }

    // 새로운 1등 유저의 돈을 Lock
    newWinner.lockPoint(request.bidAmount());

    // Item 객체 정보 업데이트
    item.updateBid(newWinner, request.bidAmount());

    // 입찰 기록 저장
    Bid bidRecord = Bid.builder()
        .item(item)
        .user(newWinner)
        .bidPrice(request.bidAmount())
        .build();
    bidRepository.save(bidRecord);

    // Outbox 테이블 기록 (랭킹 서버 전파용)
    saveOutboxEvent(item, newWinner);

    return new BidResponse(true, "입찰에 성공했습니다!", item.getCurrentPrice(), newWinner.getNickname());
  }

  private void saveOutboxEvent(Item item, User winner) {
    try {
      Map<String, Object> payloadMap = Map.of(
          "itemId", item.getId(),
          "winnerId", winner.getId(),
          "bidPrice", item.getCurrentPrice(),
          "winnerNickname", winner.getNickname()
      );
      String payload = objectMapper.writeValueAsString(payloadMap);

      Outbox outbox = Outbox.builder()
          .aggregateId(item.getId())
          .eventType(EventType.BID_PLACED)
          .payload(payload)
          .build();
      outboxRepository.save(outbox);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("이벤트 페이로드 생성 실패", e);
    }
  }
}
