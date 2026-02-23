package com.FlashBid_Main.FlashBid_Main.Notification.service;

import com.FlashBid_Main.FlashBid_Main.Auth.domain.User;
import com.FlashBid_Main.FlashBid_Main.Auth.domain.UserRole;
import com.FlashBid_Main.FlashBid_Main.Item.domain.Category;
import com.FlashBid_Main.FlashBid_Main.Item.domain.Item;
import com.FlashBid_Main.FlashBid_Main.Notification.domain.Notification;
import com.FlashBid_Main.FlashBid_Main.Notification.dto.NotificationResponse;
import com.FlashBid_Main.FlashBid_Main.Notification.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    private User seller;
    private User winner;
    private Item item;

    @BeforeEach
    void setUp() {
        seller = User.builder()
            .userId("seller1")
            .password("pw")
            .nickname("판매자")
            .role(UserRole.USER)
            .build();
        ReflectionTestUtils.setField(seller, "id", 1L);

        winner = User.builder()
            .userId("winner1")
            .password("pw")
            .nickname("낙찰자")
            .role(UserRole.USER)
            .build();
        ReflectionTestUtils.setField(winner, "id", 2L);

        item = Item.builder()
            .itemName("테스트 상품")
            .description("설명")
            .category(Category.ELECTRONICS)
            .startPrice(10000L)
            .endTime(LocalDateTime.now().minusMinutes(1))
            .seller(seller)
            .build();
        item.updateBid(winner, 15000L);
        ReflectionTestUtils.setField(item, "id", 10L);
    }

    @Nested
    @DisplayName("createAuctionEndNotifications 테스트")
    class CreateAuctionEndNotificationsTest {

        @Test
        @DisplayName("낙찰자 있을 때 save() 2회 호출 및 winner용 SUCCESS, seller용 END 알림 저장")
        void createAuctionEndNotifications_WithWinner_SavesTwoNotifications() {
            notificationService.createAuctionEndNotifications(item);

            ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
            verify(notificationRepository, times(2)).save(captor.capture());

            List<Notification> saved = captor.getAllValues();
            assertThat(saved).hasSize(2);
            assertThat(saved.get(0).getType()).isEqualTo("SUCCESS");
            assertThat(saved.get(0).getUser()).isEqualTo(winner);
            assertThat(saved.get(1).getType()).isEqualTo("END");
            assertThat(saved.get(1).getUser()).isEqualTo(seller);
        }

        @Test
        @DisplayName("낙찰자 없을 때 save() 1회 호출 및 seller용 INFO 알림 저장")
        void createAuctionEndNotifications_NoWinner_SavesOneInfoNotification() {
            Item itemNoWinner = Item.builder()
                .itemName("유찰 상품")
                .description("설명")
                .category(Category.ELECTRONICS)
                .startPrice(10000L)
                .endTime(LocalDateTime.now().minusMinutes(1))
                .seller(seller)
                .build();
            ReflectionTestUtils.setField(itemNoWinner, "id", 11L);

            notificationService.createAuctionEndNotifications(itemNoWinner);

            ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
            verify(notificationRepository, times(1)).save(captor.capture());

            Notification saved = captor.getValue();
            assertThat(saved.getType()).isEqualTo("INFO");
            assertThat(saved.getUser()).isEqualTo(seller);
        }
    }

    @Nested
    @DisplayName("getNotificationsForUser 테스트")
    class GetNotificationsForUserTest {

        @Test
        @DisplayName("userId로 알림 조회 시 NotificationResponse 리스트로 매핑되어 반환")
        void getNotificationsForUser_ReturnsNotificationResponseList() {
            Notification notification = Notification.forWinner(winner, item);
            ReflectionTestUtils.setField(notification, "id", 1L);
            ReflectionTestUtils.setField(notification, "createdAt", LocalDateTime.now());

            given(notificationRepository.findAllByUserUserIdOrderByCreatedAtDesc("winner1"))
                .willReturn(List.of(notification));

            List<NotificationResponse> result = notificationService.getNotificationsForUser("winner1");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).type()).isEqualTo("SUCCESS");
            assertThat(result.get(0).title()).isEqualTo("낙찰 완료!");
            assertThat(result.get(0).read()).isFalse();
        }
    }

    @Nested
    @DisplayName("getUnreadNotificationsForUser 테스트")
    class GetUnreadNotificationsForUserTest {

        @Test
        @DisplayName("미읽음 알림만 반환하며 올바른 repository 메서드 호출")
        void getUnreadNotificationsForUser_ReturnsUnreadOnlyWithCorrectRepositoryCall() {
            Notification notification = Notification.forSellerWithWinner(seller, item);
            ReflectionTestUtils.setField(notification, "id", 2L);
            ReflectionTestUtils.setField(notification, "createdAt", LocalDateTime.now());

            given(notificationRepository.findAllByUserUserIdAndReadFalseOrderByCreatedAtDesc("seller1"))
                .willReturn(List.of(notification));

            List<NotificationResponse> result = notificationService.getUnreadNotificationsForUser("seller1");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).type()).isEqualTo("END");
            assertThat(result.get(0).read()).isFalse();
            verify(notificationRepository).findAllByUserUserIdAndReadFalseOrderByCreatedAtDesc("seller1");
        }
    }

    @Nested
    @DisplayName("markAsRead 테스트")
    class MarkAsReadTest {

        @Test
        @DisplayName("존재하는 알림 조회 시 markAsRead() 호출")
        void markAsRead_NotificationExists_CallsMarkAsRead() {
            Notification notification = spy(Notification.forWinner(winner, item));
            given(notificationRepository.findByIdAndUserUserId(1L, "winner1"))
                .willReturn(Optional.of(notification));

            notificationService.markAsRead(1L, "winner1");

            verify(notification).markAsRead();
        }

        @Test
        @DisplayName("존재하지 않는 알림 조회 시 예외 없이 무시")
        void markAsRead_NotificationNotExists_DoesNothing() {
            given(notificationRepository.findByIdAndUserUserId(99L, "winner1"))
                .willReturn(Optional.empty());

            notificationService.markAsRead(99L, "winner1");

            verify(notificationRepository).findByIdAndUserUserId(99L, "winner1");
        }
    }
}
