package com.FlashBid_Main.FlashBid_Main.Outbox.repository;

import com.FlashBid_Main.FlashBid_Main.Outbox.domain.Outbox;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OutboxRepository extends JpaRepository<Outbox, Long> {

  List<Outbox> findAllByProcessedFalseOrderByCreatedAtAsc(Pageable pageable);
}
