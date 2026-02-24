package com.FlashBid_Main.FlashBid_Main.Auction.service;

import com.FlashBid_Main.FlashBid_Main.Item.domain.AuctionStatus;
import com.FlashBid_Main.FlashBid_Main.Item.domain.Item;
import com.FlashBid_Main.FlashBid_Main.Item.repository.ItemRepository;
import com.FlashBid_Main.FlashBid_Main.Notification.service.NotificationService;
import com.FlashBid_Main.FlashBid_Main.Outbox.domain.EventType;
import com.FlashBid_Main.FlashBid_Main.Outbox.domain.Outbox;
import com.FlashBid_Main.FlashBid_Main.Outbox.repository.OutboxRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuctionEndScheduler {

    private final ItemRepository itemRepository;
    private final NotificationService notificationService;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelay = 10_000)
    @Transactional
    public void processEndedAuctions() {
        List<Item> expiredItems = itemRepository
            .findAllByStatusAndEndTimeBefore(AuctionStatus.ONGOING, LocalDateTime.now());

        for (Item item : expiredItems) {
            try {
                item.endAuction();
                notificationService.createAuctionEndNotifications(item);
                saveAuctionEndOutboxEvent(item);
                log.info("경매 종료 처리 완료 - itemId: {}", item.getId());
            } catch (Exception e) {
                log.error("경매 종료 처리 실패 - itemId: {}, 사유: {}", item.getId(), e.getMessage());
            }
        }
    }

    private void saveAuctionEndOutboxEvent(Item item) {
        try {
            Map<String, Object> payloadMap = new HashMap<>();
            payloadMap.put("itemId", item.getId());
            payloadMap.put("itemName", item.getItemName());
            payloadMap.put("finalPrice", item.getCurrentPrice());
            if (item.getCurrentWinner() != null) {
                payloadMap.put("winnerId", item.getCurrentWinner().getId());
                payloadMap.put("winnerNickname", item.getCurrentWinner().getNickname());
            }
            String payload = objectMapper.writeValueAsString(payloadMap);

            Outbox outbox = Outbox.builder()
                .aggregateId(item.getId())
                .eventType(EventType.AUCTION_END)
                .payload(payload)
                .build();
            outboxRepository.save(outbox);
        } catch (JsonProcessingException e) {
            log.error("경매 종료 이벤트 페이로드 생성 실패 - itemId: {}", item.getId(), e);
        }
    }
}
