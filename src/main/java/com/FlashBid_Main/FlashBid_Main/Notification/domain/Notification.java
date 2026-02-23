package com.FlashBid_Main.FlashBid_Main.Notification.domain;

import com.FlashBid_Main.FlashBid_Main.Auth.domain.User;
import com.FlashBid_Main.FlashBid_Main.Item.domain.Item;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    private String targetUrl;

    @Column(name = "is_read", nullable = false)
    private boolean read = false;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @Builder
    private Notification(User user, String type, String title, String message, String targetUrl) {
        this.user = user;
        this.type = type;
        this.title = title;
        this.message = message;
        this.targetUrl = targetUrl;
        this.read = false;
    }

    public static Notification forWinner(User winner, Item item) {
        return Notification.builder()
            .user(winner)
            .type("SUCCESS")
            .title("낙찰 완료!")
            .message(String.format("[%s]에 %,d원으로 낙찰되셨습니다", item.getItemName(), item.getCurrentPrice()))
            .targetUrl("/auctions/" + item.getId())
            .build();
    }

    public static Notification forSellerWithWinner(User seller, Item item) {
        return Notification.builder()
            .user(seller)
            .type("END")
            .title("경매 종료")
            .message(String.format("[%s] 경매가 종료되었습니다. 낙찰자: %s, 낙찰가: %,d원",
                item.getItemName(), item.getCurrentWinner().getNickname(), item.getCurrentPrice()))
            .targetUrl("/auctions/" + item.getId())
            .build();
    }

    public static Notification forSellerNoWinner(User seller, Item item) {
        return Notification.builder()
            .user(seller)
            .type("INFO")
            .title("경매 유찰")
            .message(String.format("[%s] 경매가 유찰되었습니다. 입찰자가 없었습니다", item.getItemName()))
            .targetUrl("/auctions/" + item.getId())
            .build();
    }

    public void markAsRead() {
        this.read = true;
    }
}
