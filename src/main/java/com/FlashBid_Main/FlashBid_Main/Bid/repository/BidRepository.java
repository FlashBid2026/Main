package com.FlashBid_Main.FlashBid_Main.Bid.repository;

import com.FlashBid_Main.FlashBid_Main.Bid.domain.Bid;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BidRepository extends JpaRepository<Bid, Long> {
}
