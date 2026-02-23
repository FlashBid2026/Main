package com.FlashBid_Main.FlashBid_Main.Notification.repository;

import com.FlashBid_Main.FlashBid_Main.Notification.domain.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findAllByUserUserIdOrderByCreatedAtDesc(String userId);

    List<Notification> findAllByUserUserIdAndReadFalseOrderByCreatedAtDesc(String userId);

    Optional<Notification> findByIdAndUserUserId(Long id, String userId);
}
