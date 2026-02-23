package com.FlashBid_Main.FlashBid_Main.Notification.service;

import com.FlashBid_Main.FlashBid_Main.Auth.domain.User;
import com.FlashBid_Main.FlashBid_Main.Item.domain.Item;
import com.FlashBid_Main.FlashBid_Main.Notification.domain.Notification;
import com.FlashBid_Main.FlashBid_Main.Notification.dto.NotificationResponse;
import com.FlashBid_Main.FlashBid_Main.Notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional
    public void createAuctionEndNotifications(Item item) {
        User seller = item.getSeller();
        User winner = item.getCurrentWinner();

        if (winner != null) {
            notificationRepository.save(Notification.forWinner(winner, item));
            notificationRepository.save(Notification.forSellerWithWinner(seller, item));
        } else {
            notificationRepository.save(Notification.forSellerNoWinner(seller, item));
        }
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotificationsForUser(String userId) {
        return notificationRepository.findAllByUserUserIdOrderByCreatedAtDesc(userId)
            .stream()
            .map(NotificationResponse::from)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getUnreadNotificationsForUser(String userId) {
        return notificationRepository.findAllByUserUserIdAndReadFalseOrderByCreatedAtDesc(userId)
            .stream()
            .map(NotificationResponse::from)
            .collect(Collectors.toList());
    }

    @Transactional
    public void markAsRead(Long notificationId, String userId) {
        notificationRepository.findByIdAndUserUserId(notificationId, userId)
            .ifPresent(Notification::markAsRead);
    }
}
