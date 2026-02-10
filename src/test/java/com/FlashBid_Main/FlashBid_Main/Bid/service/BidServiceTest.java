package com.FlashBid_Main.FlashBid_Main.Bid.service;

import com.FlashBid_Main.FlashBid_Main.Auth.domain.User;
import com.FlashBid_Main.FlashBid_Main.Auth.domain.UserRole;
import com.FlashBid_Main.FlashBid_Main.Auth.repository.UserRepository;
import com.FlashBid_Main.FlashBid_Main.Bid.domain.Bid;
import com.FlashBid_Main.FlashBid_Main.Bid.dto.BidRequest;
import com.FlashBid_Main.FlashBid_Main.Bid.dto.BidResponse;
import com.FlashBid_Main.FlashBid_Main.Bid.repository.BidRepository;
import com.FlashBid_Main.FlashBid_Main.Item.domain.Category;
import com.FlashBid_Main.FlashBid_Main.Item.domain.Item;
import com.FlashBid_Main.FlashBid_Main.Item.repository.ItemRepository;
import com.FlashBid_Main.FlashBid_Main.Outbox.domain.EventType;
import com.FlashBid_Main.FlashBid_Main.Outbox.domain.Outbox;
import com.FlashBid_Main.FlashBid_Main.Outbox.repository.OutboxRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BidServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BidRepository bidRepository;

    @Mock
    private OutboxRepository outboxRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private BidService bidService;

    private User testUser;
    private User previousWinner;
    private Item testItem;
    private BidRequest bidRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
            .userId("test@test.com")
            .password("password123")
            .nickname("testUser")
            .availablePoint(100000L)
            .role(UserRole.USER)
            .build();
        ReflectionTestUtils.setField(testUser, "id", 1L);

        previousWinner = User.builder()
            .userId("previous@test.com")
            .password("password123")
            .nickname("previousWinner")
            .availablePoint(50000L)
            .role(UserRole.USER)
            .build();
        ReflectionTestUtils.setField(previousWinner, "id", 2L);

        User seller = User.builder()
            .userId("seller@test.com")
            .password("password123")
            .nickname("seller")
            .role(UserRole.USER)
            .build();

        testItem = Item.builder()
            .itemName("테스트 상품")
            .description("테스트 상품 설명")
            .category(Category.ELECTRONICS)
            .startPrice(10000L)
            .endTime(LocalDateTime.now().plusHours(24))
            .seller(seller)
            .build();
        ReflectionTestUtils.setField(testItem, "id", 1L);

        bidRequest = new BidRequest(1L, 15000L);
    }

    @Nested
    @DisplayName("placeBid 성공 케이스")
    class PlaceBidSuccessTest {

        @Test
        @DisplayName("정상 입찰 시 성공 응답 반환")
        void placeBid_ValidBid_ReturnsSuccessResponse() throws JsonProcessingException {
            given(itemRepository.findByIdWithPessimisticLock(1L)).willReturn(Optional.of(testItem));
            given(userRepository.findByUserId("test@test.com")).willReturn(Optional.of(testUser));
            given(objectMapper.writeValueAsString(any())).willReturn("{}");

            BidResponse response = bidService.placeBid(bidRequest, "test@test.com");

            assertThat(response.success()).isTrue();
            assertThat(response.message()).isEqualTo("입찰에 성공했습니다!");
            assertThat(response.currentPrice()).isEqualTo(15000L);
            assertThat(response.winnerNickname()).isEqualTo("testUser");
        }

        @Test
        @DisplayName("기존 입찰자가 있을 때 포인트 잠금 해제 검증")
        void placeBid_WithPreviousWinner_UnlocksPreviousPoints() throws JsonProcessingException {
            previousWinner.lockPoint(10000L);
            testItem.updateBid(previousWinner, 10000L);

            Long previousLockedPoint = previousWinner.getLockedPoint();

            given(itemRepository.findByIdWithPessimisticLock(1L)).willReturn(Optional.of(testItem));
            given(userRepository.findByUserId("test@test.com")).willReturn(Optional.of(testUser));
            given(objectMapper.writeValueAsString(any())).willReturn("{}");

            bidService.placeBid(bidRequest, "test@test.com");

            assertThat(previousWinner.getLockedPoint()).isEqualTo(previousLockedPoint - 10000L);
            assertThat(previousWinner.getAvailablePoint()).isEqualTo(50000L);
        }

        @Test
        @DisplayName("새 입찰자 포인트 잠금 검증")
        void placeBid_ValidBid_LocksNewWinnerPoints() throws JsonProcessingException {
            Long initialAvailablePoint = testUser.getAvailablePoint();
            Long initialLockedPoint = testUser.getLockedPoint();

            given(itemRepository.findByIdWithPessimisticLock(1L)).willReturn(Optional.of(testItem));
            given(userRepository.findByUserId("test@test.com")).willReturn(Optional.of(testUser));
            given(objectMapper.writeValueAsString(any())).willReturn("{}");

            bidService.placeBid(bidRequest, "test@test.com");

            assertThat(testUser.getAvailablePoint()).isEqualTo(initialAvailablePoint - 15000L);
            assertThat(testUser.getLockedPoint()).isEqualTo(initialLockedPoint + 15000L);
        }

        @Test
        @DisplayName("Item.updateBid() 호출 검증")
        void placeBid_ValidBid_UpdatesItemBidInfo() throws JsonProcessingException {
            given(itemRepository.findByIdWithPessimisticLock(1L)).willReturn(Optional.of(testItem));
            given(userRepository.findByUserId("test@test.com")).willReturn(Optional.of(testUser));
            given(objectMapper.writeValueAsString(any())).willReturn("{}");

            bidService.placeBid(bidRequest, "test@test.com");

            assertThat(testItem.getCurrentPrice()).isEqualTo(15000L);
            assertThat(testItem.getCurrentWinner()).isEqualTo(testUser);
        }

        @Test
        @DisplayName("Bid 기록 저장 검증")
        void placeBid_ValidBid_SavesBidRecord() throws JsonProcessingException {
            given(itemRepository.findByIdWithPessimisticLock(1L)).willReturn(Optional.of(testItem));
            given(userRepository.findByUserId("test@test.com")).willReturn(Optional.of(testUser));
            given(objectMapper.writeValueAsString(any())).willReturn("{}");

            bidService.placeBid(bidRequest, "test@test.com");

            ArgumentCaptor<Bid> bidCaptor = ArgumentCaptor.forClass(Bid.class);
            verify(bidRepository).save(bidCaptor.capture());

            Bid savedBid = bidCaptor.getValue();
            assertThat(savedBid.getItem()).isEqualTo(testItem);
            assertThat(savedBid.getUser()).isEqualTo(testUser);
            assertThat(savedBid.getBidPrice()).isEqualTo(15000L);
        }

        @Test
        @DisplayName("Outbox 이벤트 저장 검증")
        void placeBid_ValidBid_SavesOutboxEvent() throws JsonProcessingException {
            given(itemRepository.findByIdWithPessimisticLock(1L)).willReturn(Optional.of(testItem));
            given(userRepository.findByUserId("test@test.com")).willReturn(Optional.of(testUser));
            given(objectMapper.writeValueAsString(any())).willReturn("{}");

            bidService.placeBid(bidRequest, "test@test.com");

            ArgumentCaptor<Outbox> outboxCaptor = ArgumentCaptor.forClass(Outbox.class);
            verify(outboxRepository).save(outboxCaptor.capture());

            Outbox savedOutbox = outboxCaptor.getValue();
            assertThat(savedOutbox.getAggregateId()).isEqualTo(testItem.getId());
            assertThat(savedOutbox.getEventType()).isEqualTo(EventType.BID_PLACED);
        }
    }

    @Nested
    @DisplayName("placeBid 경매 종료 검증")
    class PlaceBidAuctionEndTest {

        @Test
        @DisplayName("종료된 경매에 실패 응답 반환")
        void placeBid_AuctionEnded_ReturnsFailure() {
            ReflectionTestUtils.setField(testItem, "endTime", LocalDateTime.now().minusHours(1));

            given(itemRepository.findByIdWithPessimisticLock(1L)).willReturn(Optional.of(testItem));

            BidResponse response = bidService.placeBid(bidRequest, "test@test.com");

            assertThat(response.success()).isFalse();
            assertThat(response.message()).isEqualTo("경매가 이미 종료되었습니다.");
        }

        @Test
        @DisplayName("종료된 경매 시 포인트/기록 변경 없음")
        void placeBid_AuctionEnded_DoesNotModifyAnything() {
            ReflectionTestUtils.setField(testItem, "endTime", LocalDateTime.now().minusHours(1));
            Long initialPrice = testItem.getCurrentPrice();

            given(itemRepository.findByIdWithPessimisticLock(1L)).willReturn(Optional.of(testItem));

            bidService.placeBid(bidRequest, "test@test.com");

            assertThat(testItem.getCurrentPrice()).isEqualTo(initialPrice);
            verify(userRepository, never()).findByUserId(any());
            verify(bidRepository, never()).save(any());
            verify(outboxRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("placeBid 금액 검증")
    class PlaceBidAmountValidationTest {

        @Test
        @DisplayName("현재가보다 낮은 금액 입찰 실패")
        void placeBid_BidAmountLessThanCurrentPrice_ReturnsFailure() {
            testItem.updateBid(previousWinner, 20000L);
            BidRequest lowBidRequest = new BidRequest(1L, 15000L);

            given(itemRepository.findByIdWithPessimisticLock(1L)).willReturn(Optional.of(testItem));

            BidResponse response = bidService.placeBid(lowBidRequest, "test@test.com");

            assertThat(response.success()).isFalse();
            assertThat(response.message()).isEqualTo("현재 최고가보다 높은 금액을 입찰해야 합니다.");
            assertThat(response.currentPrice()).isEqualTo(20000L);
        }

        @Test
        @DisplayName("현재가와 같은 금액 입찰 실패")
        void placeBid_BidAmountEqualToCurrentPrice_ReturnsFailure() {
            BidRequest equalBidRequest = new BidRequest(1L, 10000L);

            given(itemRepository.findByIdWithPessimisticLock(1L)).willReturn(Optional.of(testItem));

            BidResponse response = bidService.placeBid(equalBidRequest, "test@test.com");

            assertThat(response.success()).isFalse();
            assertThat(response.message()).isEqualTo("현재 최고가보다 높은 금액을 입찰해야 합니다.");
        }
    }

    @Nested
    @DisplayName("placeBid 엔티티 미존재")
    class PlaceBidEntityNotFoundTest {

        @Test
        @DisplayName("상품 미존재 시 예외 발생")
        void placeBid_ItemNotFound_ThrowsException() {
            given(itemRepository.findByIdWithPessimisticLock(1L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> bidService.placeBid(bidRequest, "test@test.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 상품입니다.");
        }

        @Test
        @DisplayName("유저 미존재 시 예외 발생")
        void placeBid_UserNotFound_ThrowsException() {
            given(itemRepository.findByIdWithPessimisticLock(1L)).willReturn(Optional.of(testItem));
            given(userRepository.findByUserId("test@test.com")).willReturn(Optional.empty());

            assertThatThrownBy(() -> bidService.placeBid(bidRequest, "test@test.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("유저를 찾을 수 없습니다.");
        }
    }

    @Nested
    @DisplayName("saveOutboxEvent 테스트")
    class SaveOutboxEventTest {

        @Test
        @DisplayName("Outbox payload에 올바른 데이터 포함")
        void placeBid_OutboxEventContainsCorrectPayload() throws JsonProcessingException {
            given(itemRepository.findByIdWithPessimisticLock(1L)).willReturn(Optional.of(testItem));
            given(userRepository.findByUserId("test@test.com")).willReturn(Optional.of(testUser));

            String expectedPayload = "{\"itemId\":1,\"winnerId\":1,\"bidPrice\":15000,\"winnerNickname\":\"testUser\"}";
            given(objectMapper.writeValueAsString(any())).willReturn(expectedPayload);

            bidService.placeBid(bidRequest, "test@test.com");

            ArgumentCaptor<Outbox> outboxCaptor = ArgumentCaptor.forClass(Outbox.class);
            verify(outboxRepository).save(outboxCaptor.capture());

            Outbox savedOutbox = outboxCaptor.getValue();
            assertThat(savedOutbox.getPayload()).isEqualTo(expectedPayload);
        }

        @Test
        @DisplayName("JSON 직렬화 실패 시 RuntimeException 발생")
        void placeBid_JsonProcessingException_ThrowsRuntimeException() throws JsonProcessingException {
            given(itemRepository.findByIdWithPessimisticLock(1L)).willReturn(Optional.of(testItem));
            given(userRepository.findByUserId("test@test.com")).willReturn(Optional.of(testUser));
            given(objectMapper.writeValueAsString(any())).willThrow(new JsonProcessingException("직렬화 실패") {});

            assertThatThrownBy(() -> bidService.placeBid(bidRequest, "test@test.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("이벤트 페이로드 생성 실패");
        }
    }
}
