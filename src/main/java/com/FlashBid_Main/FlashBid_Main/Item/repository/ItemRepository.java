package com.FlashBid_Main.FlashBid_Main.Item.repository;

import com.FlashBid_Main.FlashBid_Main.Item.domain.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface ItemRepository extends JpaRepository<Item, Long> {

  Page<Item> findByEndTimeAfterOrderByEndTimeAsc(LocalDateTime now, Pageable pageable);

  Page<Item> findBySellerIdOrderByIdDesc(Long sellerId, Pageable pageable);
}