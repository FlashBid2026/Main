package com.FlashBid_Main.FlashBid_Main.Item.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ItemImageTest {

    @Nested
    @DisplayName("ItemImage 생성 테스트")
    class ItemImageCreationTest {

        @Test
        @DisplayName("생성자로 gcsFileName 설정")
        void constructor_GcsFileNameSet() {
            String gcsFileName = "uuid-test-image.jpg";

            ItemImage image = new ItemImage(gcsFileName);

            assertThat(image.getGcsFileName()).isEqualTo(gcsFileName);
        }

        @Test
        @DisplayName("기본 생성자 동작 확인")
        void noArgsConstructor_Works() {
            ItemImage image = new ItemImage();

            assertThat(image.getId()).isNull();
            assertThat(image.getGcsFileName()).isNull();
            assertThat(image.getImageUrl()).isNull();
            assertThat(image.getItem()).isNull();
        }
    }

    @Nested
    @DisplayName("setItem 테스트")
    class SetItemTest {

        @Test
        @DisplayName("setItem으로 Item 관계 설정")
        void setItem_RelationshipSet() {
            Item item = Item.builder()
                .itemName("테스트 상품")
                .description("테스트 설명")
                .startPrice(10000L)
                .endTime(LocalDateTime.now().plusHours(24))
                .seller(null)
                .build();

            ItemImage image = new ItemImage("test-image.jpg");

            image.setItem(item);

            assertThat(image.getItem()).isEqualTo(item);
        }

        @Test
        @DisplayName("setItem을 null로 호출해도 정상 동작")
        void setItem_NullAllowed() {
            ItemImage image = new ItemImage("test-image.jpg");

            image.setItem(null);

            assertThat(image.getItem()).isNull();
        }
    }
}
