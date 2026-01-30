package com.FlashBid_Main.FlashBid_Main.Item.domain;

import com.FlashBid_Main.FlashBid_Main.Auth.domain.User;
import com.FlashBid_Main.FlashBid_Main.Auth.domain.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ItemTest {

    @Nested
    @DisplayName("Item 생성 테스트")
    class ItemCreationTest {

        @Test
        @DisplayName("Builder로 객체 생성 시 모든 필드 정상 초기화")
        void builder_AllFieldsInitialized() {
            User seller = User.builder()
                .userId("seller@test.com")
                .password("password123")
                .nickname("seller")
                .role(UserRole.USER)
                .build();

            LocalDateTime endTime = LocalDateTime.now().plusHours(24);

            Item item = Item.builder()
                .itemName("테스트 상품")
                .description("테스트 상품 설명입니다.")
                .startPrice(10000L)
                .endTime(endTime)
                .seller(seller)
                .build();

            assertThat(item.getItemName()).isEqualTo("테스트 상품");
            assertThat(item.getDescription()).isEqualTo("테스트 상품 설명입니다.");
            assertThat(item.getStartPrice()).isEqualTo(10000L);
            assertThat(item.getEndTime()).isEqualTo(endTime);
            assertThat(item.getSeller()).isEqualTo(seller);
        }

        @Test
        @DisplayName("currentPrice가 startPrice로 자동 설정")
        void builder_CurrentPriceSetToStartPrice() {
            LocalDateTime endTime = LocalDateTime.now().plusHours(24);

            Item item = Item.builder()
                .itemName("테스트 상품")
                .description("테스트 상품 설명")
                .startPrice(50000L)
                .endTime(endTime)
                .seller(null)
                .build();

            assertThat(item.getCurrentPrice()).isEqualTo(50000L);
            assertThat(item.getCurrentPrice()).isEqualTo(item.getStartPrice());
        }

        @Test
        @DisplayName("images 리스트 초기화 확인")
        void builder_ImagesListInitialized() {
            LocalDateTime endTime = LocalDateTime.now().plusHours(24);

            Item item = Item.builder()
                .itemName("테스트 상품")
                .description("테스트 상품 설명")
                .startPrice(10000L)
                .endTime(endTime)
                .seller(null)
                .build();

            assertThat(item.getImages()).isNotNull();
            assertThat(item.getImages()).isEmpty();
        }
    }

    @Nested
    @DisplayName("addImage 테스트")
    class AddImageTest {

        @Test
        @DisplayName("addImage 호출 시 양방향 관계 설정")
        void addImage_BidirectionalRelationshipSet() {
            LocalDateTime endTime = LocalDateTime.now().plusHours(24);

            Item item = Item.builder()
                .itemName("테스트 상품")
                .description("테스트 상품 설명")
                .startPrice(10000L)
                .endTime(endTime)
                .seller(null)
                .build();

            ItemImage image = new ItemImage("test-image.jpg");

            item.addImage(image);

            assertThat(item.getImages()).hasSize(1);
            assertThat(item.getImages().get(0)).isEqualTo(image);
            assertThat(image.getItem()).isEqualTo(item);
        }

        @Test
        @DisplayName("여러 이미지 추가 시 모두 정상 등록")
        void addImage_MultipleImages() {
            LocalDateTime endTime = LocalDateTime.now().plusHours(24);

            Item item = Item.builder()
                .itemName("테스트 상품")
                .description("테스트 상품 설명")
                .startPrice(10000L)
                .endTime(endTime)
                .seller(null)
                .build();

            ItemImage image1 = new ItemImage("image1.jpg");
            ItemImage image2 = new ItemImage("image2.jpg");
            ItemImage image3 = new ItemImage("image3.jpg");

            item.addImage(image1);
            item.addImage(image2);
            item.addImage(image3);

            assertThat(item.getImages()).hasSize(3);
            assertThat(item.getImages()).containsExactly(image1, image2, image3);
            assertThat(image1.getItem()).isEqualTo(item);
            assertThat(image2.getItem()).isEqualTo(item);
            assertThat(image3.getItem()).isEqualTo(item);
        }
    }
}
