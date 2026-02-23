package com.FlashBid_Main.FlashBid_Main.Notification.domain;

import com.FlashBid_Main.FlashBid_Main.Auth.domain.User;
import com.FlashBid_Main.FlashBid_Main.Auth.domain.UserRole;
import com.FlashBid_Main.FlashBid_Main.Item.domain.Category;
import com.FlashBid_Main.FlashBid_Main.Item.domain.Item;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationTest {

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
    @DisplayName("forWinner 테스트")
    class ForWinnerTest {

        @Test
        @DisplayName("낙찰자용 알림의 type이 SUCCESS")
        void forWinner_TypeIsSuccess() {
            Notification notification = Notification.forWinner(winner, item);
            assertThat(notification.getType()).isEqualTo("SUCCESS");
        }

        @Test
        @DisplayName("낙찰자용 알림의 title이 '낙찰 완료!'")
        void forWinner_TitleIsCorrect() {
            Notification notification = Notification.forWinner(winner, item);
            assertThat(notification.getTitle()).isEqualTo("낙찰 완료!");
        }

        @Test
        @DisplayName("낙찰자용 알림의 message에 itemName과 currentPrice 포함")
        void forWinner_MessageContainsItemNameAndPrice() {
            Notification notification = Notification.forWinner(winner, item);
            assertThat(notification.getMessage()).contains("테스트 상품");
            assertThat(notification.getMessage()).contains("15,000");
        }

        @Test
        @DisplayName("낙찰자용 알림의 targetUrl이 /auctions/{id}")
        void forWinner_TargetUrlIsCorrect() {
            Notification notification = Notification.forWinner(winner, item);
            assertThat(notification.getTargetUrl()).isEqualTo("/auctions/10");
        }

        @Test
        @DisplayName("낙찰자용 알림 생성 시 read가 false")
        void forWinner_ReadIsFalse() {
            Notification notification = Notification.forWinner(winner, item);
            assertThat(notification.isRead()).isFalse();
        }

        @Test
        @DisplayName("낙찰자용 알림의 user가 winner")
        void forWinner_UserIsWinner() {
            Notification notification = Notification.forWinner(winner, item);
            assertThat(notification.getUser()).isEqualTo(winner);
        }
    }

    @Nested
    @DisplayName("forSellerWithWinner 테스트")
    class ForSellerWithWinnerTest {

        @Test
        @DisplayName("낙찰자 있는 경우 판매자 알림의 type이 END")
        void forSellerWithWinner_TypeIsEnd() {
            Notification notification = Notification.forSellerWithWinner(seller, item);
            assertThat(notification.getType()).isEqualTo("END");
        }

        @Test
        @DisplayName("낙찰자 있는 경우 판매자 알림 message에 winnerNickname과 currentPrice 포함")
        void forSellerWithWinner_MessageContainsWinnerAndPrice() {
            Notification notification = Notification.forSellerWithWinner(seller, item);
            assertThat(notification.getMessage()).contains("낙찰자");
            assertThat(notification.getMessage()).contains("15,000");
        }

        @Test
        @DisplayName("낙찰자 있는 경우 판매자 알림의 user가 seller")
        void forSellerWithWinner_UserIsSeller() {
            Notification notification = Notification.forSellerWithWinner(seller, item);
            assertThat(notification.getUser()).isEqualTo(seller);
        }
    }

    @Nested
    @DisplayName("forSellerNoWinner 테스트")
    class ForSellerNoWinnerTest {

        @Test
        @DisplayName("낙찰자 없는 경우 판매자 알림의 type이 INFO")
        void forSellerNoWinner_TypeIsInfo() {
            Notification notification = Notification.forSellerNoWinner(seller, item);
            assertThat(notification.getType()).isEqualTo("INFO");
        }

        @Test
        @DisplayName("낙찰자 없는 경우 판매자 알림의 title이 '경매 유찰'")
        void forSellerNoWinner_TitleIsCorrect() {
            Notification notification = Notification.forSellerNoWinner(seller, item);
            assertThat(notification.getTitle()).isEqualTo("경매 유찰");
        }

        @Test
        @DisplayName("낙찰자 없는 경우 판매자 알림 message에 itemName 포함")
        void forSellerNoWinner_MessageContainsItemName() {
            Notification notification = Notification.forSellerNoWinner(seller, item);
            assertThat(notification.getMessage()).contains("테스트 상품");
        }
    }

    @Nested
    @DisplayName("markAsRead 테스트")
    class MarkAsReadTest {

        @Test
        @DisplayName("markAsRead 호출 시 read가 false에서 true로 변경")
        void markAsRead_ChangesReadToTrue() {
            Notification notification = Notification.forWinner(winner, item);
            assertThat(notification.isRead()).isFalse();

            notification.markAsRead();

            assertThat(notification.isRead()).isTrue();
        }
    }
}
