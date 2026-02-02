package com.FlashBid_Main.FlashBid_Main.Item.domain;

import com.FlashBid_Main.FlashBid_Main.Auth.domain.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Item {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String itemName;

  @Column(nullable = false)
  private String description;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Category category;

  @Column(nullable = false)
  private Long startPrice;

  private Long currentPrice;

  @Column(nullable = false)
  private LocalDateTime endTime;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "seller_id")
  private User seller;

  @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<ItemImage> images = new ArrayList<>();

  @CreatedDate
  private LocalDateTime createdAt;

  @Builder
  public Item(String itemName, String description, Category category, Long startPrice, LocalDateTime endTime, User seller) {
    this.itemName = itemName;
    this.description = description;
    this.category = category;
    this.startPrice = startPrice;
    this.currentPrice = startPrice;
    this.endTime = endTime;
    this.seller = seller;
  }

  public void addImage(ItemImage image) {
    this.images.add(image);
    image.setItem(this);
  }
}
