package com.personal.marketnote.commerce.service.order;

import com.personal.marketnote.commerce.domain.order.*;
import com.personal.marketnote.commerce.exception.InvalidOrderStatusTransitionException;
import com.personal.marketnote.commerce.exception.OrderStatusAlreadyChangedException;
import com.personal.marketnote.commerce.exception.UnauthorizedOrderAccessException;
import com.personal.marketnote.commerce.port.in.command.order.CancelOrderCommand;
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

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CancelOrderUseCaseTest {
    @Mock
    private GetOrderUseCase getOrderUseCase;
    @Mock
    private UpdateOrderPort updateOrderPort;
    @Spy
    private Clock clock = Clock.fixed(Instant.parse("2026-04-05T00:00:00Z"), ZoneId.of("Asia/Seoul"));

    @InjectMocks
    private CancelOrderService cancelOrderService;

    // ==================================================================================
    // 정상 취소 요청
    // ==================================================================================

    @Nested
    @DisplayName("정상 취소 요청")
    class SuccessfulCancelRequestTest {

        @Test
        @DisplayName("결제 대기 상태의 주문을 취소 요청하면 정상 처리된다")
        void cancelOrder_fromPaymentPending_succeeds() {
            Long orderId = 1L;
            Long buyerId = 100L;
            Order order = createOrderWithBuyerId(orderId, buyerId, OrderStatus.PAYMENT_PENDING);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            CancelOrderCommand command = CancelOrderCommand.builder()
                    .id(orderId)
                    .reasonCategory(OrderStatusReasonCategory.CANCEL_ORDER)
                    .reason("구매 의사 취소")
                    .buyerId(buyerId)
                    .build();

            assertThatCode(() -> cancelOrderService.cancelOrder(command))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("결제 완료 상태의 주문을 취소 요청하면 정상 처리된다")
        void cancelOrder_fromPaid_succeeds() {
            Long orderId = 1L;
            Long buyerId = 100L;
            Order order = createOrderWithBuyerId(orderId, buyerId, OrderStatus.PAID);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            CancelOrderCommand command = CancelOrderCommand.builder()
                    .id(orderId)
                    .reasonCategory(OrderStatusReasonCategory.MISTAKE)
                    .buyerId(buyerId)
                    .build();

            assertThatCode(() -> cancelOrderService.cancelOrder(command))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("상품 준비중 상태의 주문을 취소 요청하면 정상 처리된다")
        void cancelOrder_fromPreparing_succeeds() {
            Long orderId = 1L;
            Long buyerId = 100L;
            Order order = createOrderWithBuyerId(orderId, buyerId, OrderStatus.PREPARING);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            CancelOrderCommand command = CancelOrderCommand.builder()
                    .id(orderId)
                    .buyerId(buyerId)
                    .build();

            assertThatCode(() -> cancelOrderService.cancelOrder(command))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("취소 요청 성공 시 UpdateOrderPort를 호출한다")
        void cancelOrder_callsUpdateOrderPort() {
            Long orderId = 1L;
            Long buyerId = 100L;
            Order order = createOrderWithBuyerId(orderId, buyerId, OrderStatus.PAID);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            CancelOrderCommand command = CancelOrderCommand.builder()
                    .id(orderId)
                    .reasonCategory(OrderStatusReasonCategory.CANCEL_ORDER)
                    .reason("구매 의사 취소")
                    .buyerId(buyerId)
                    .build();

            cancelOrderService.cancelOrder(command);

            verify(updateOrderPort).update(any(Order.class), any(OrderStatusHistory.class));
        }
    }

    // ==================================================================================
    // 구매자 소유권 검증
    // ==================================================================================

    @Nested
    @DisplayName("구매자 소유권 검증")
    class BuyerOwnershipValidationTest {

        @Test
        @DisplayName("타인의 주문을 취소 요청하면 UnauthorizedOrderAccessException이 발생한다")
        void cancelOrder_otherBuyerOrder_throwsException() {
            Long orderId = 1L;
            Long ownerBuyerId = 100L;
            Long attackerBuyerId = 999L;
            Order order = createOrderWithBuyerId(orderId, ownerBuyerId, OrderStatus.PAID);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            CancelOrderCommand command = CancelOrderCommand.builder()
                    .id(orderId)
                    .buyerId(attackerBuyerId)
                    .build();

            assertThatThrownBy(() -> cancelOrderService.cancelOrder(command))
                    .isInstanceOf(UnauthorizedOrderAccessException.class);
        }

        @Test
        @DisplayName("타인의 주문 취소 요청 시 UpdateOrderPort를 호출하지 않는다")
        void cancelOrder_otherBuyerOrder_doesNotCallUpdatePort() {
            Long orderId = 1L;
            Long ownerBuyerId = 100L;
            Long attackerBuyerId = 999L;
            Order order = createOrderWithBuyerId(orderId, ownerBuyerId, OrderStatus.PAID);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            CancelOrderCommand command = CancelOrderCommand.builder()
                    .id(orderId)
                    .buyerId(attackerBuyerId)
                    .build();

            assertThatThrownBy(() -> cancelOrderService.cancelOrder(command))
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
        @DisplayName("이미 취소된 주문을 다시 취소 요청하면 OrderStatusAlreadyChangedException이 발생한다")
        void cancelOrder_alreadyCancelled_throwsAlreadyChangedException() {
            Long orderId = 1L;
            Long buyerId = 100L;
            Order order = createOrderWithBuyerId(orderId, buyerId, OrderStatus.CANCELLED);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            CancelOrderCommand command = CancelOrderCommand.builder()
                    .id(orderId)
                    .buyerId(buyerId)
                    .build();

            assertThatThrownBy(() -> cancelOrderService.cancelOrder(command))
                    .isInstanceOf(OrderStatusAlreadyChangedException.class);
        }

        @Test
        @DisplayName("배송중 상태의 주문을 취소 요청하면 InvalidOrderStatusTransitionException이 발생한다")
        void cancelOrder_fromShipping_throwsException() {
            Long orderId = 1L;
            Long buyerId = 100L;
            Order order = createOrderWithBuyerId(orderId, buyerId, OrderStatus.SHIPPING);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            CancelOrderCommand command = CancelOrderCommand.builder()
                    .id(orderId)
                    .buyerId(buyerId)
                    .build();

            assertThatThrownBy(() -> cancelOrderService.cancelOrder(command))
                    .isInstanceOf(InvalidOrderStatusTransitionException.class);
        }

        @Test
        @DisplayName("배송 완료 상태의 주문을 취소 요청하면 InvalidOrderStatusTransitionException이 발생한다")
        void cancelOrder_fromDelivered_throwsException() {
            Long orderId = 1L;
            Long buyerId = 100L;
            Order order = createOrderWithBuyerId(orderId, buyerId, OrderStatus.DELIVERED);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            CancelOrderCommand command = CancelOrderCommand.builder()
                    .id(orderId)
                    .buyerId(buyerId)
                    .build();

            assertThatThrownBy(() -> cancelOrderService.cancelOrder(command))
                    .isInstanceOf(InvalidOrderStatusTransitionException.class);
        }

        @Test
        @DisplayName("구매 확정 상태의 주문을 취소 요청하면 InvalidOrderStatusTransitionException이 발생한다")
        void cancelOrder_fromConfirmed_throwsException() {
            Long orderId = 1L;
            Long buyerId = 100L;
            Order order = createOrderWithBuyerId(orderId, buyerId, OrderStatus.CONFIRMED);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            CancelOrderCommand command = CancelOrderCommand.builder()
                    .id(orderId)
                    .buyerId(buyerId)
                    .build();

            assertThatThrownBy(() -> cancelOrderService.cancelOrder(command))
                    .isInstanceOf(InvalidOrderStatusTransitionException.class);
        }

        @Test
        @DisplayName("상태 전이 실패 시 UpdateOrderPort를 호출하지 않는다")
        void cancelOrder_invalidTransition_doesNotCallUpdatePort() {
            Long orderId = 1L;
            Long buyerId = 100L;
            Order order = createOrderWithBuyerId(orderId, buyerId, OrderStatus.SHIPPING);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            CancelOrderCommand command = CancelOrderCommand.builder()
                    .id(orderId)
                    .buyerId(buyerId)
                    .build();

            assertThatThrownBy(() -> cancelOrderService.cancelOrder(command))
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
