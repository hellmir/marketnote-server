package com.personal.marketnote.commerce.service.order;

import com.personal.marketnote.commerce.domain.order.*;
import com.personal.marketnote.commerce.exception.InvalidOrderStatusTransitionException;
import com.personal.marketnote.commerce.exception.InvalidReasonCategoryException;
import com.personal.marketnote.commerce.exception.OrderStatusAlreadyChangedException;
import com.personal.marketnote.commerce.exception.UnauthorizedOrderAccessException;
import com.personal.marketnote.commerce.port.in.command.order.RequestReturnCommand;
import com.personal.marketnote.commerce.port.in.usecase.order.GetOrderUseCase;
import com.personal.marketnote.commerce.port.out.order.UpdateOrderPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RequestReturnUseCaseTest {
    @Mock
    private GetOrderUseCase getOrderUseCase;
    @Mock
    private UpdateOrderPort updateOrderPort;
    @Spy
    private Clock clock = Clock.fixed(Instant.parse("2026-04-05T00:00:00Z"), ZoneId.of("Asia/Seoul"));

    @InjectMocks
    private RequestReturnService requestReturnService;

    // ==================================================================================
    // 정상 반품 요청
    // ==================================================================================

    @Nested
    @DisplayName("정상 반품 요청")
    class SuccessfulReturnRequestTest {

        @Test
        @DisplayName("배송 완료 상태의 주문을 반품 요청하면 정상 처리된다")
        void requestReturn_fromDelivered_succeeds() {
            Long orderId = 1L;
            Long buyerId = 100L;
            Order order = createOrderWithBuyerId(orderId, buyerId, OrderStatus.DELIVERED);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            RequestReturnCommand command = RequestReturnCommand.builder()
                    .id(orderId)
                    .reasonCategory(OrderStatusReasonCategory.ETC)
                    .reason("상품 불량")
                    .buyerId(buyerId)
                    .build();

            assertThatCode(() -> requestReturnService.requestReturn(command))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("배송중 상태의 주문을 반품 요청하면 정상 처리된다")
        void requestReturn_fromShipping_succeeds() {
            Long orderId = 1L;
            Long buyerId = 100L;
            Order order = createOrderWithBuyerId(orderId, buyerId, OrderStatus.SHIPPING);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            RequestReturnCommand command = RequestReturnCommand.builder()
                    .id(orderId)
                    .reasonCategory(OrderStatusReasonCategory.ETC)
                    .reason("상품 불량")
                    .buyerId(buyerId)
                    .build();

            assertThatCode(() -> requestReturnService.requestReturn(command))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("부분 구매 확정 상태의 주문을 반품 요청하면 정상 처리된다")
        void requestReturn_fromPartiallyConfirmed_succeeds() {
            Long orderId = 1L;
            Long buyerId = 100L;
            Order order = createOrderWithBuyerId(orderId, buyerId, OrderStatus.PARTIALLY_CONFIRMED);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            RequestReturnCommand command = RequestReturnCommand.builder()
                    .id(orderId)
                    .reasonCategory(OrderStatusReasonCategory.ETC)
                    .buyerId(buyerId)
                    .build();

            assertThatCode(() -> requestReturnService.requestReturn(command))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("부분 반품됨 상태의 주문을 반품 요청하면 정상 처리된다")
        void requestReturn_fromPartiallyReturned_succeeds() {
            Long orderId = 1L;
            Long buyerId = 100L;
            Order order = createOrderWithBuyerId(orderId, buyerId, OrderStatus.PARTIALLY_RETURNED);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            RequestReturnCommand command = RequestReturnCommand.builder()
                    .id(orderId)
                    .buyerId(buyerId)
                    .build();

            assertThatCode(() -> requestReturnService.requestReturn(command))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("반품 요청 성공 시 UpdateOrderPort를 호출한다")
        void requestReturn_callsUpdateOrderPort() {
            Long orderId = 1L;
            Long buyerId = 100L;
            Order order = createOrderWithBuyerId(orderId, buyerId, OrderStatus.DELIVERED);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            RequestReturnCommand command = RequestReturnCommand.builder()
                    .id(orderId)
                    .reasonCategory(OrderStatusReasonCategory.ETC)
                    .reason("상품 불량")
                    .buyerId(buyerId)
                    .build();

            requestReturnService.requestReturn(command);

            verify(updateOrderPort).update(any(Order.class), any(OrderStatusHistory.class));
        }

        @Test
        @DisplayName("회수지 주소를 입력하면 입력된 회수지가 적용된다")
        void requestReturn_withPickupAddress_appliesProvidedAddress() {
            Long orderId = 1L;
            Long buyerId = 100L;
            Order order = createOrderWithBuyerId(orderId, buyerId, OrderStatus.DELIVERED);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            RequestReturnCommand command = RequestReturnCommand.builder()
                    .id(orderId)
                    .reasonCategory(OrderStatusReasonCategory.ETC)
                    .reason("상품 불량")
                    .buyerId(buyerId)
                    .pickupRecipientName("회수 수령인")
                    .pickupRecipientPhoneNumber("01099998888")
                    .pickupZipCode("54321")
                    .pickupAddress("회수지 주소")
                    .pickupAddressDetail("회수지 상세주소")
                    .pickupRequestMessage("부재시 경비실에 맡겨주세요")
                    .build();

            requestReturnService.requestReturn(command);

            assertThat(order.getPickupAddress().getRecipientName()).isEqualTo("회수 수령인");
            assertThat(order.getPickupAddress().getRecipientPhoneNumber()).isEqualTo("01099998888");
            assertThat(order.getPickupAddress().getZipCode()).isEqualTo("54321");
            assertThat(order.getPickupAddress().getAddress()).isEqualTo("회수지 주소");
            assertThat(order.getPickupAddress().getAddressDetail()).isEqualTo("회수지 상세주소");
            assertThat(order.getPickupAddress().getDeliveryRequestMessage()).isEqualTo("부재시 경비실에 맡겨주세요");
        }

        @Test
        @DisplayName("회수지 주소를 미입력하면 배송지가 회수지 기본값으로 적용된다")
        void requestReturn_withoutPickupAddress_appliesShippingAddressAsDefault() {
            Long orderId = 1L;
            Long buyerId = 100L;
            Order order = createOrderWithBuyerId(orderId, buyerId, OrderStatus.DELIVERED);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            RequestReturnCommand command = RequestReturnCommand.builder()
                    .id(orderId)
                    .reasonCategory(OrderStatusReasonCategory.ETC)
                    .buyerId(buyerId)
                    .build();

            requestReturnService.requestReturn(command);

            assertThat(order.getPickupAddress().getRecipientName()).isEqualTo("수령인");
            assertThat(order.getPickupAddress().getRecipientPhoneNumber()).isEqualTo("01012345678");
            assertThat(order.getPickupAddress().getZipCode()).isEqualTo("12345");
            assertThat(order.getPickupAddress().getAddress()).isEqualTo("서울시 강남구");
            assertThat(order.getPickupAddress().getAddressDetail()).isEqualTo("상세주소");
        }
    }

    // ==================================================================================
    // 반품 사유 카테고리 검증
    // ==================================================================================

    @Nested
    @DisplayName("반품 사유 카테고리 검증")
    class ReturnReasonCategoryValidationTest {

        @Test
        @DisplayName("반품 전용 사유로 반품 요청하면 정상 처리된다")
        void requestReturn_withReturnReason_succeeds() {
            Long orderId = 1L;
            Long buyerId = 100L;
            Order order = createOrderWithBuyerId(orderId, buyerId, OrderStatus.DELIVERED);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            RequestReturnCommand command = RequestReturnCommand.builder()
                    .id(orderId)
                    .reasonCategory(OrderStatusReasonCategory.SIMPLE_CHANGE_OF_MIND)
                    .buyerId(buyerId)
                    .build();

            assertThatCode(() -> requestReturnService.requestReturn(command))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("공용 사유(ETC)로 반품 요청하면 정상 처리된다")
        void requestReturn_withBothReason_succeeds() {
            Long orderId = 1L;
            Long buyerId = 100L;
            Order order = createOrderWithBuyerId(orderId, buyerId, OrderStatus.DELIVERED);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            RequestReturnCommand command = RequestReturnCommand.builder()
                    .id(orderId)
                    .reasonCategory(OrderStatusReasonCategory.ETC)
                    .reason("기타 사유")
                    .buyerId(buyerId)
                    .build();

            assertThatCode(() -> requestReturnService.requestReturn(command))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("취소 전용 사유로 반품 요청하면 InvalidReasonCategoryException이 발생한다")
        void requestReturn_withCancelOnlyReason_throwsException() {
            Long orderId = 1L;
            Long buyerId = 100L;
            Order order = createOrderWithBuyerId(orderId, buyerId, OrderStatus.DELIVERED);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            RequestReturnCommand command = RequestReturnCommand.builder()
                    .id(orderId)
                    .reasonCategory(OrderStatusReasonCategory.CANCEL_ORDER)
                    .buyerId(buyerId)
                    .build();

            assertThatThrownBy(() -> requestReturnService.requestReturn(command))
                    .isInstanceOf(InvalidReasonCategoryException.class);

            verifyNoInteractions(updateOrderPort);
        }

        @Test
        @DisplayName("사유 카테고리가 null이면 검증을 건너뛴다")
        void requestReturn_withNullReasonCategory_succeeds() {
            Long orderId = 1L;
            Long buyerId = 100L;
            Order order = createOrderWithBuyerId(orderId, buyerId, OrderStatus.DELIVERED);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            RequestReturnCommand command = RequestReturnCommand.builder()
                    .id(orderId)
                    .buyerId(buyerId)
                    .build();

            assertThatCode(() -> requestReturnService.requestReturn(command))
                    .doesNotThrowAnyException();
        }
    }

    // ==================================================================================
    // 구매자 소유권 검증
    // ==================================================================================

    @Nested
    @DisplayName("구매자 소유권 검증")
    class BuyerOwnershipValidationTest {

        @Test
        @DisplayName("타인의 주문을 반품 요청하면 UnauthorizedOrderAccessException이 발생한다")
        void requestReturn_otherBuyerOrder_throwsException() {
            Long orderId = 1L;
            Long ownerBuyerId = 100L;
            Long attackerBuyerId = 999L;
            Order order = createOrderWithBuyerId(orderId, ownerBuyerId, OrderStatus.DELIVERED);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            RequestReturnCommand command = RequestReturnCommand.builder()
                    .id(orderId)
                    .buyerId(attackerBuyerId)
                    .build();

            assertThatThrownBy(() -> requestReturnService.requestReturn(command))
                    .isInstanceOf(UnauthorizedOrderAccessException.class);
        }

        @Test
        @DisplayName("타인의 주문 반품 요청 시 UpdateOrderPort를 호출하지 않는다")
        void requestReturn_otherBuyerOrder_doesNotCallUpdatePort() {
            Long orderId = 1L;
            Long ownerBuyerId = 100L;
            Long attackerBuyerId = 999L;
            Order order = createOrderWithBuyerId(orderId, ownerBuyerId, OrderStatus.DELIVERED);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            RequestReturnCommand command = RequestReturnCommand.builder()
                    .id(orderId)
                    .buyerId(attackerBuyerId)
                    .build();

            assertThatThrownBy(() -> requestReturnService.requestReturn(command))
                    .isInstanceOf(UnauthorizedOrderAccessException.class);

            verifyNoInteractions(updateOrderPort);
        }
    }

    // ==================================================================================
    // 상태 전이 검증
    // ==================================================================================

    @Nested
    @DisplayName("상태 전이 검증")
    class StatusTransitionValidationTest {

        @Test
        @DisplayName("이미 반품 요청된 주문을 다시 반품 요청하면 OrderStatusAlreadyChangedException이 발생한다")
        void requestReturn_alreadyReturnRequested_throwsException() {
            Long orderId = 1L;
            Long buyerId = 100L;
            Order order = createOrderWithBuyerId(orderId, buyerId, OrderStatus.RETURN_REQUESTED);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            RequestReturnCommand command = RequestReturnCommand.builder()
                    .id(orderId)
                    .buyerId(buyerId)
                    .build();

            assertThatThrownBy(() -> requestReturnService.requestReturn(command))
                    .isInstanceOf(OrderStatusAlreadyChangedException.class);
        }

        @Test
        @DisplayName("결제 대기 상태의 주문을 반품 요청하면 InvalidOrderStatusTransitionException이 발생한다")
        void requestReturn_fromPaymentPending_throwsException() {
            Long orderId = 1L;
            Long buyerId = 100L;
            Order order = createOrderWithBuyerId(orderId, buyerId, OrderStatus.PAYMENT_PENDING);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            RequestReturnCommand command = RequestReturnCommand.builder()
                    .id(orderId)
                    .buyerId(buyerId)
                    .build();

            assertThatThrownBy(() -> requestReturnService.requestReturn(command))
                    .isInstanceOf(InvalidOrderStatusTransitionException.class);
        }

        @Test
        @DisplayName("상품 준비중 상태의 주문을 반품 요청하면 InvalidOrderStatusTransitionException이 발생한다")
        void requestReturn_fromPreparing_throwsException() {
            Long orderId = 1L;
            Long buyerId = 100L;
            Order order = createOrderWithBuyerId(orderId, buyerId, OrderStatus.PREPARING);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            RequestReturnCommand command = RequestReturnCommand.builder()
                    .id(orderId)
                    .buyerId(buyerId)
                    .build();

            assertThatThrownBy(() -> requestReturnService.requestReturn(command))
                    .isInstanceOf(InvalidOrderStatusTransitionException.class);
        }

        @Test
        @DisplayName("구매 확정 상태의 주문을 반품 요청하면 InvalidOrderStatusTransitionException이 발생한다")
        void requestReturn_fromConfirmed_throwsException() {
            Long orderId = 1L;
            Long buyerId = 100L;
            Order order = createOrderWithBuyerId(orderId, buyerId, OrderStatus.CONFIRMED);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            RequestReturnCommand command = RequestReturnCommand.builder()
                    .id(orderId)
                    .buyerId(buyerId)
                    .build();

            assertThatThrownBy(() -> requestReturnService.requestReturn(command))
                    .isInstanceOf(InvalidOrderStatusTransitionException.class);
        }

        @Test
        @DisplayName("상태 전이 실패 시 UpdateOrderPort를 호출하지 않는다")
        void requestReturn_invalidTransition_doesNotCallUpdatePort() {
            Long orderId = 1L;
            Long buyerId = 100L;
            Order order = createOrderWithBuyerId(orderId, buyerId, OrderStatus.PREPARING);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            RequestReturnCommand command = RequestReturnCommand.builder()
                    .id(orderId)
                    .buyerId(buyerId)
                    .build();

            assertThatThrownBy(() -> requestReturnService.requestReturn(command))
                    .isInstanceOf(InvalidOrderStatusTransitionException.class);

            verifyNoInteractions(updateOrderPort);
        }
    }

    // ==================================================================================
    // 헬퍼 메서드
    // ==================================================================================

    private Order createOrderWithBuyerId(Long orderId, Long buyerId, OrderStatus status) {
        List<OrderProductSnapshotState> productStates = List.of(
                OrderProductSnapshotState.builder()
                        .orderId(orderId)
                        .sellerId(10L)
                        .pricePolicyId(100L)
                        .quantity(1)
                        .unitAmount(50000L)
                        .orderStatus(status)
                        .build()
        );

        return Order.from(OrderSnapshotState.builder()
                .id(orderId)
                .buyerId(buyerId)
                .orderKey(UUID.randomUUID())
                .orderNumber("ORD-" + orderId)
                .orderStatus(status)
                .amount(OrderAmount.of(50000L, null, 0L, 0L, null))
                .shippingAddress(ShippingAddress.of("수령인", "01012345678", "12345", "서울시 강남구", "상세주소", null, null))
                .orderProductStates(productStates)
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build());
    }
}
