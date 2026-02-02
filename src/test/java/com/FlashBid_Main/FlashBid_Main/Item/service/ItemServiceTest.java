package com.FlashBid_Main.FlashBid_Main.Item.service;

import com.FlashBid_Main.FlashBid_Main.Auth.domain.User;
import com.FlashBid_Main.FlashBid_Main.Auth.domain.UserRole;
import com.FlashBid_Main.FlashBid_Main.Gcs.service.GcsService;
import com.FlashBid_Main.FlashBid_Main.Item.domain.Category;
import com.FlashBid_Main.FlashBid_Main.Item.domain.Item;
import com.FlashBid_Main.FlashBid_Main.Item.dto.ItemRegistrationDto;
import com.FlashBid_Main.FlashBid_Main.Item.repository.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private GcsService gcsService;

    @InjectMocks
    private ItemService itemService;

    private User testSeller;
    private ItemRegistrationDto dto;

    @BeforeEach
    void setUp() {
        testSeller = User.builder()
            .userId("seller@test.com")
            .password("password123")
            .nickname("seller")
            .role(UserRole.USER)
            .build();

        dto = new ItemRegistrationDto();
        dto.setItemName("테스트 상품");
        dto.setDescription("테스트 상품 설명입니다.");
        dto.setStartPrice(10000L);
        dto.setDurationHour(24);
        dto.setCategory(Category.ELECTRONICS);
    }

    @Nested
    @DisplayName("registerItem 테스트")
    class RegisterItemTest {

        @Test
        @DisplayName("정상적인 물품 등록 (이미지 포함)")
        void registerItem_WithImages_Success() throws IOException {
            MockMultipartFile image1 = new MockMultipartFile(
                "images", "image1.jpg", "image/jpeg", "test image 1".getBytes()
            );
            MockMultipartFile image2 = new MockMultipartFile(
                "images", "image2.jpg", "image/jpeg", "test image 2".getBytes()
            );
            dto.setImages(List.of(image1, image2));

            when(gcsService.uploadImage(any(MultipartFile.class)))
                .thenReturn("uuid-image1.jpg")
                .thenReturn("uuid-image2.jpg");

            when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> {
                Item item = invocation.getArgument(0);
                return item;
            });

            Long result = itemService.registerItem(dto, testSeller);

            verify(gcsService, times(2)).uploadImage(any(MultipartFile.class));
            verify(itemRepository, times(1)).save(any(Item.class));

            ArgumentCaptor<Item> itemCaptor = ArgumentCaptor.forClass(Item.class);
            verify(itemRepository).save(itemCaptor.capture());
            Item savedItem = itemCaptor.getValue();

            assertThat(savedItem.getItemName()).isEqualTo("테스트 상품");
            assertThat(savedItem.getImages()).hasSize(2);
        }

        @Test
        @DisplayName("이미지 없이 물품 등록")
        void registerItem_WithoutImages_Success() throws IOException {
            dto.setImages(null);

            when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> {
                Item item = invocation.getArgument(0);
                return item;
            });

            itemService.registerItem(dto, testSeller);

            verify(gcsService, never()).uploadImage(any(MultipartFile.class));
            verify(itemRepository, times(1)).save(any(Item.class));

            ArgumentCaptor<Item> itemCaptor = ArgumentCaptor.forClass(Item.class);
            verify(itemRepository).save(itemCaptor.capture());
            Item savedItem = itemCaptor.getValue();

            assertThat(savedItem.getImages()).isEmpty();
        }

        @Test
        @DisplayName("빈 이미지 리스트로 물품 등록")
        void registerItem_WithEmptyImagesList_Success() throws IOException {
            dto.setImages(new ArrayList<>());

            when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> {
                Item item = invocation.getArgument(0);
                return item;
            });

            itemService.registerItem(dto, testSeller);

            verify(gcsService, never()).uploadImage(any(MultipartFile.class));
            verify(itemRepository, times(1)).save(any(Item.class));
        }

        @Test
        @DisplayName("빈 파일을 포함한 이미지 리스트에서 빈 파일 제외")
        void registerItem_WithEmptyFile_SkipsEmptyFiles() throws IOException {
            MockMultipartFile validImage = new MockMultipartFile(
                "images", "image.jpg", "image/jpeg", "valid content".getBytes()
            );
            MockMultipartFile emptyImage = new MockMultipartFile(
                "images", "empty.jpg", "image/jpeg", new byte[0]
            );
            dto.setImages(List.of(validImage, emptyImage));

            when(gcsService.uploadImage(any(MultipartFile.class))).thenReturn("uuid-image.jpg");
            when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> invocation.getArgument(0));

            itemService.registerItem(dto, testSeller);

            verify(gcsService, times(1)).uploadImage(any(MultipartFile.class));
        }

        @Test
        @DisplayName("endTime 계산 검증 (durationHour 적용)")
        void registerItem_EndTimeCalculation() throws IOException {
            dto.setDurationHour(48);
            dto.setImages(null);

            LocalDateTime beforeCall = LocalDateTime.now();

            when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> invocation.getArgument(0));

            itemService.registerItem(dto, testSeller);

            LocalDateTime afterCall = LocalDateTime.now();

            ArgumentCaptor<Item> itemCaptor = ArgumentCaptor.forClass(Item.class);
            verify(itemRepository).save(itemCaptor.capture());
            Item savedItem = itemCaptor.getValue();

            assertThat(savedItem.getEndTime()).isAfter(beforeCall.plusHours(47));
            assertThat(savedItem.getEndTime()).isBefore(afterCall.plusHours(49));
        }

        @Test
        @DisplayName("currentPrice가 startPrice로 설정되는지 검증")
        void registerItem_CurrentPriceEqualsStartPrice() throws IOException {
            dto.setStartPrice(50000L);
            dto.setImages(null);

            when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> invocation.getArgument(0));

            itemService.registerItem(dto, testSeller);

            ArgumentCaptor<Item> itemCaptor = ArgumentCaptor.forClass(Item.class);
            verify(itemRepository).save(itemCaptor.capture());
            Item savedItem = itemCaptor.getValue();

            assertThat(savedItem.getCurrentPrice()).isEqualTo(50000L);
            assertThat(savedItem.getCurrentPrice()).isEqualTo(savedItem.getStartPrice());
        }

        @Test
        @DisplayName("GcsService.uploadImage() 호출 검증")
        void registerItem_GcsServiceCalled() throws IOException {
            MockMultipartFile image = new MockMultipartFile(
                "images", "test.jpg", "image/jpeg", "content".getBytes()
            );
            dto.setImages(List.of(image));

            when(gcsService.uploadImage(image)).thenReturn("uploaded-file-name.jpg");
            when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> invocation.getArgument(0));

            itemService.registerItem(dto, testSeller);

            verify(gcsService).uploadImage(image);
        }

        @Test
        @DisplayName("ItemRepository.save() 호출 검증")
        void registerItem_RepositorySaveCalled() throws IOException {
            dto.setImages(null);

            when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> invocation.getArgument(0));

            itemService.registerItem(dto, testSeller);

            verify(itemRepository, times(1)).save(any(Item.class));
        }

        @Test
        @DisplayName("IOException 발생 시 예외 전파")
        void registerItem_ThrowsIOException_WhenGcsServiceFails() throws IOException {
            MockMultipartFile image = new MockMultipartFile(
                "images", "test.jpg", "image/jpeg", "content".getBytes()
            );
            dto.setImages(List.of(image));

            when(gcsService.uploadImage(any(MultipartFile.class)))
                .thenThrow(new IOException("GCS 업로드 실패"));

            assertThatThrownBy(() -> itemService.registerItem(dto, testSeller))
                .isInstanceOf(IOException.class)
                .hasMessage("GCS 업로드 실패");

            verify(itemRepository, never()).save(any(Item.class));
        }

        @Test
        @DisplayName("판매자 정보가 Item에 정상 설정")
        void registerItem_SellerInfoSet() throws IOException {
            dto.setImages(null);

            when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> invocation.getArgument(0));

            itemService.registerItem(dto, testSeller);

            ArgumentCaptor<Item> itemCaptor = ArgumentCaptor.forClass(Item.class);
            verify(itemRepository).save(itemCaptor.capture());
            Item savedItem = itemCaptor.getValue();

            assertThat(savedItem.getSeller()).isEqualTo(testSeller);
        }
    }
}
