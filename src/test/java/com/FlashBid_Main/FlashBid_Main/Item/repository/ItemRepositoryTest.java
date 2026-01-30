package com.FlashBid_Main.FlashBid_Main.Item.repository;

import com.FlashBid_Main.FlashBid_Main.Auth.domain.User;
import com.FlashBid_Main.FlashBid_Main.Auth.domain.UserRole;
import com.FlashBid_Main.FlashBid_Main.Auth.repository.UserRepository;
import com.FlashBid_Main.FlashBid_Main.Item.domain.Item;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ItemRepositoryTest {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager em;

    private User seller1;
    private User seller2;

    @BeforeEach
    void setUp() {
        seller1 = User.builder()
            .userId("seller1@test.com")
            .password("password123")
            .nickname("seller1")
            .role(UserRole.USER)
            .build();
        seller1 = userRepository.save(seller1);

        seller2 = User.builder()
            .userId("seller2@test.com")
            .password("password123")
            .nickname("seller2")
            .role(UserRole.USER)
            .build();
        seller2 = userRepository.save(seller2);
    }

    @AfterEach
    void tearDown() {
        itemRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Nested
    @DisplayName("findByEndTimeAfterOrderByEndTimeAsc 테스트")
    class FindByEndTimeAfterOrderByEndTimeAscTest {

        @Test
        @DisplayName("활성 경매만 조회 (endTime이 현재 이후인 것만)")
        void findActiveAuctions_OnlyReturnsActiveItems() {
            LocalDateTime now = LocalDateTime.now();

            Item activeItem1 = Item.builder()
                .itemName("활성 상품 1")
                .description("설명")
                .startPrice(10000L)
                .endTime(now.plusHours(1))
                .seller(seller1)
                .build();

            Item activeItem2 = Item.builder()
                .itemName("활성 상품 2")
                .description("설명")
                .startPrice(20000L)
                .endTime(now.plusHours(2))
                .seller(seller1)
                .build();

            Item expiredItem = Item.builder()
                .itemName("만료된 상품")
                .description("설명")
                .startPrice(30000L)
                .endTime(now.minusHours(1))
                .seller(seller1)
                .build();

            itemRepository.saveAll(List.of(activeItem1, activeItem2, expiredItem));

            Pageable pageable = PageRequest.of(0, 10);
            Page<Item> result = itemRepository.findByEndTimeAfterOrderByEndTimeAsc(now, pageable);

            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent())
                .extracting(Item::getItemName)
                .containsExactlyInAnyOrder("활성 상품 1", "활성 상품 2");
        }

        @Test
        @DisplayName("endTime 오름차순 정렬 확인")
        void findActiveAuctions_OrderedByEndTimeAsc() {
            LocalDateTime now = LocalDateTime.now();

            Item item1 = Item.builder()
                .itemName("마감 임박 상품")
                .description("설명")
                .startPrice(10000L)
                .endTime(now.plusHours(1))
                .seller(seller1)
                .build();

            Item item2 = Item.builder()
                .itemName("여유 있는 상품")
                .description("설명")
                .startPrice(20000L)
                .endTime(now.plusHours(5))
                .seller(seller1)
                .build();

            Item item3 = Item.builder()
                .itemName("중간 상품")
                .description("설명")
                .startPrice(15000L)
                .endTime(now.plusHours(3))
                .seller(seller1)
                .build();

            itemRepository.saveAll(List.of(item1, item2, item3));

            Pageable pageable = PageRequest.of(0, 10);
            Page<Item> result = itemRepository.findByEndTimeAfterOrderByEndTimeAsc(now, pageable);

            assertThat(result.getContent()).hasSize(3);
            assertThat(result.getContent().get(0).getItemName()).isEqualTo("마감 임박 상품");
            assertThat(result.getContent().get(1).getItemName()).isEqualTo("중간 상품");
            assertThat(result.getContent().get(2).getItemName()).isEqualTo("여유 있는 상품");
        }

        @Test
        @DisplayName("페이징 동작 확인")
        void findActiveAuctions_PagingWorks() {
            LocalDateTime now = LocalDateTime.now();

            for (int i = 1; i <= 15; i++) {
                Item item = Item.builder()
                    .itemName("상품 " + i)
                    .description("설명")
                    .startPrice(10000L * i)
                    .endTime(now.plusHours(i))
                    .seller(seller1)
                    .build();
                itemRepository.save(item);
            }

            Page<Item> page1 = itemRepository.findByEndTimeAfterOrderByEndTimeAsc(now, PageRequest.of(0, 5));
            Page<Item> page2 = itemRepository.findByEndTimeAfterOrderByEndTimeAsc(now, PageRequest.of(1, 5));
            Page<Item> page3 = itemRepository.findByEndTimeAfterOrderByEndTimeAsc(now, PageRequest.of(2, 5));

            assertThat(page1.getContent()).hasSize(5);
            assertThat(page2.getContent()).hasSize(5);
            assertThat(page3.getContent()).hasSize(5);
            assertThat(page1.getTotalElements()).isEqualTo(15);
            assertThat(page1.getTotalPages()).isEqualTo(3);
        }

        @Test
        @DisplayName("만료된 경매 제외 확인")
        void findActiveAuctions_ExcludesExpiredItems() {
            LocalDateTime now = LocalDateTime.now();

            Item expiredItem1 = Item.builder()
                .itemName("만료 상품 1")
                .description("설명")
                .startPrice(10000L)
                .endTime(now.minusHours(1))
                .seller(seller1)
                .build();

            Item expiredItem2 = Item.builder()
                .itemName("만료 상품 2")
                .description("설명")
                .startPrice(20000L)
                .endTime(now.minusDays(1))
                .seller(seller1)
                .build();

            itemRepository.saveAll(List.of(expiredItem1, expiredItem2));

            Pageable pageable = PageRequest.of(0, 10);
            Page<Item> result = itemRepository.findByEndTimeAfterOrderByEndTimeAsc(now, pageable);

            assertThat(result.getContent()).isEmpty();
        }
    }

    @Nested
    @DisplayName("findBySellerIdOrderByIdDesc 테스트")
    class FindBySellerIdOrderByIdDescTest {

        @Test
        @DisplayName("특정 판매자의 물품만 조회")
        void findBySellerId_OnlyReturnsSellersItems() {
            LocalDateTime now = LocalDateTime.now();

            Item seller1Item1 = Item.builder()
                .itemName("판매자1 상품1")
                .description("설명")
                .startPrice(10000L)
                .endTime(now.plusHours(1))
                .seller(seller1)
                .build();

            Item seller1Item2 = Item.builder()
                .itemName("판매자1 상품2")
                .description("설명")
                .startPrice(20000L)
                .endTime(now.plusHours(2))
                .seller(seller1)
                .build();

            Item seller2Item = Item.builder()
                .itemName("판매자2 상품")
                .description("설명")
                .startPrice(30000L)
                .endTime(now.plusHours(3))
                .seller(seller2)
                .build();

            itemRepository.saveAll(List.of(seller1Item1, seller1Item2, seller2Item));

            Pageable pageable = PageRequest.of(0, 10);
            Page<Item> result = itemRepository.findBySellerIdOrderByIdDesc(seller1.getId(), pageable);

            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent())
                .extracting(Item::getItemName)
                .containsExactlyInAnyOrder("판매자1 상품1", "판매자1 상품2");
        }

        @Test
        @DisplayName("id 내림차순 정렬 확인")
        void findBySellerId_OrderedByIdDesc() throws InterruptedException {
            LocalDateTime now = LocalDateTime.now();

            Item item1 = Item.builder()
                .itemName("먼저 등록한 상품")
                .description("설명")
                .startPrice(10000L)
                .endTime(now.plusHours(1))
                .seller(seller1)
                .build();
            itemRepository.save(item1);

            Thread.sleep(100);

            Item item2 = Item.builder()
                .itemName("나중에 등록한 상품")
                .description("설명")
                .startPrice(20000L)
                .endTime(now.plusHours(2))
                .seller(seller1)
                .build();
            itemRepository.save(item2);

            Pageable pageable = PageRequest.of(0, 10);
            Page<Item> result = itemRepository.findBySellerIdOrderByIdDesc(seller1.getId(), pageable);

            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent().get(0).getItemName()).isEqualTo("나중에 등록한 상품");
            assertThat(result.getContent().get(1).getItemName()).isEqualTo("먼저 등록한 상품");
        }

        @Test
        @DisplayName("페이징 동작 확인")
        void findBySellerId_PagingWorks() {
            LocalDateTime now = LocalDateTime.now();

            for (int i = 1; i <= 12; i++) {
                Item item = Item.builder()
                    .itemName("상품 " + i)
                    .description("설명")
                    .startPrice(10000L * i)
                    .endTime(now.plusHours(i))
                    .seller(seller1)
                    .build();
                itemRepository.save(item);
            }

            Page<Item> page1 = itemRepository.findBySellerIdOrderByIdDesc(seller1.getId(), PageRequest.of(0, 5));
            Page<Item> page2 = itemRepository.findBySellerIdOrderByIdDesc(seller1.getId(), PageRequest.of(1, 5));

            assertThat(page1.getContent()).hasSize(5);
            assertThat(page2.getContent()).hasSize(5);
            assertThat(page1.getTotalElements()).isEqualTo(12);
            assertThat(page1.getTotalPages()).isEqualTo(3);
        }

        @Test
        @DisplayName("다른 판매자 물품 제외 확인")
        void findBySellerId_ExcludesOtherSellersItems() {
            LocalDateTime now = LocalDateTime.now();

            Item seller2Item1 = Item.builder()
                .itemName("판매자2 상품1")
                .description("설명")
                .startPrice(10000L)
                .endTime(now.plusHours(1))
                .seller(seller2)
                .build();

            Item seller2Item2 = Item.builder()
                .itemName("판매자2 상품2")
                .description("설명")
                .startPrice(20000L)
                .endTime(now.plusHours(2))
                .seller(seller2)
                .build();

            itemRepository.saveAll(List.of(seller2Item1, seller2Item2));

            Pageable pageable = PageRequest.of(0, 10);
            Page<Item> result = itemRepository.findBySellerIdOrderByIdDesc(seller1.getId(), pageable);

            assertThat(result.getContent()).isEmpty();
        }
    }
}
