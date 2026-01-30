package com.FlashBid_Main.FlashBid_Main.Item.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class ItemImage {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String gcsFileName;
  private String imageUrl;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "item_id")
  private Item item;

  public void setItem(Item item) { this.item = item; }

  public ItemImage(String gcsFileName) {
    this.gcsFileName = gcsFileName;
  }
}
