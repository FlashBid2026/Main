package com.FlashBid_Main.FlashBid_Main.Outbox.repository;

import com.FlashBid_Main.FlashBid_Main.Outbox.domain.Outbox;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OutboxRepository extends JpaRepository<Outbox, Long> {
}
