package com.FlashBid_Main.FlashBid_Main.Item.repository;

import com.FlashBid_Main.FlashBid_Main.Item.domain.Item;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item, Long> {

  Page<Item> findByEndTimeAfterOrderByEndTimeAsc(LocalDateTime now, Pageable pageable);

  Page<Item> findBySellerIdOrderByIdDesc(Long sellerId, Pageable pageable);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select i from Item i where i.id = :id")
  Optional<Item> findByIdWithPessimisticLock(Long id);
}