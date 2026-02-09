package com.FlashBid_Main.FlashBid_Main.Outbox.service;

import com.FlashBid_Main.FlashBid_Main.Config.RabbitConfig;
import com.FlashBid_Main.FlashBid_Main.Outbox.domain.Outbox;
import com.FlashBid_Main.FlashBid_Main.Outbox.repository.OutboxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxRelayService {

  private final OutboxRepository outboxRepository;
  private final RabbitTemplate rabbitTemplate;

  private static final int BATCH_SIZE = 100;

  @Scheduled(fixedDelay = 500)
  @Transactional
  public void publishEvents() {
    PageRequest pageRequest = PageRequest.of(0, BATCH_SIZE);
    List<Outbox> unprocessedEvents = outboxRepository.findAllByProcessedFalseOrderByCreatedAtAsc(pageRequest);

    if (unprocessedEvents.isEmpty()) {
      return;
    }

    log.info("배치 처리 시작: {}건의 이벤트 발행 시도", unprocessedEvents.size());

    for (Outbox outbox : unprocessedEvents) {
      try {
        String routingKey = "auction.bid." + outbox.getEventType().name().toLowerCase();
        rabbitTemplate.convertAndSend(
            RabbitConfig.EXCHANGE_NAME,
            routingKey,
            outbox.getPayload()
        );

        outbox.complete();

      } catch (Exception e) {
        log.error("이벤트 발행 실패 - ID: {}, 사유: {}", outbox.getId(), e.getMessage());
      }
    }

    log.info("배치 처리 완료: {}건 처리됨", unprocessedEvents.size());
  }
}