package com.personal.marketnote.commerce.service.order;

import com.personal.marketnote.commerce.domain.order.*;
import com.personal.marketnote.commerce.exception.InvalidOrderStatusTransitionException;
import com.personal.marketnote.commerce.exception.OrderStatusAlreadyChangedException;
import com.personal.marketnote.commerce.exception.UnauthorizedOrderAccessException;
import com.personal.marketnote.commerce.port.in.command.order.ConfirmOrderCommand;
import com.personal.marketnote.commerce.port.in.usecase.order.GetOrderUseCase;
import com.personal.marketnote.commerce.port.out.event.PublishOrderEventPort;
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
class ConfirmOrderUseCaseTest {
    @Mock
    private GetOrderUseCase getOrderUseCase;
    @Mock
    private UpdateOrderPort updateOrderPort;
    @Mock
    private PublishOrderEventPort publishOrderEventPort;
    @Spy
    private Clock clock = Clock.fixed(Instant.parse("2026-04-05T00:00:00Z"), ZoneId.of("Asia/Seoul"));

    @InjectMocks
    private ConfirmOrderService confirmOrderService;

    // ==================================================================================
    // 정상 구매 확정
    // ==================================================================================

    @Nested
    @DisplayName("정상 구매 확정")
    class SuccessfulConfirmTest {

        @Test
        @DisplayName("배송 완료 상태의 주문을 구매 확정하면 정상 처리된다")
        void confirmOrder_fromDelivered_succeeds() {
            Long orderId = 1L;
            Long buyerId = 100L;
            Order order = createOrderWithBuyerId(orderId, buyerId, OrderStatus.DELIVERED);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            ConfirmOrderCommand command = ConfirmOrderCommand.builder()
                    .id(orderId)
                    .buyerId(buyerId)
                    .build();

            assertThatCode(() -> confirmOrderService.confirmOrder(command))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("부분 구매 확정 상태의 주문을 구매 확정하면 정상 처리된다")
        void confirmOrder_fromPartiallyConfirmed_succeeds() {
            Long orderId = 1L;
            Long buyerId = 100L;
            Order order = createOrderWithBuyerId(orderId, buyerId, OrderStatus.PARTIALLY_CONFIRMED);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            ConfirmOrderCommand command = ConfirmOrderCommand.builder()
                    .id(orderId)
                    .buyerId(buyerId)
                    .build();

            assertThatCode(() -> confirmOrderService.confirmOrder(command))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("구매 확정 성공 시 UpdateOrderPort를 호출한다")
        void confirmOrder_callsUpdateOrderPort() {
            Long orderId = 1L;
            Long buyerId = 100L;
            Order order = createOrderWithBuyerId(orderId, buyerId, OrderStatus.DELIVERED);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            ConfirmOrderCommand command = ConfirmOrderCommand.builder()
                    .id(orderId)
                    .buyerId(buyerId)
                    .build();

            confirmOrderService.confirmOrder(command);

            verify(updateOrderPort).update(any(Order.class), any(OrderStatusHistory.class));
        }

        @Test
        @DisplayName("구매 확정 성공 시 구매 확정 이벤트를 발행한다")
        void confirmOrder_publishesConfirmedEvent() {
            Long orderId = 1L;
            Long buyerId = 100L;
            Order order = createOrderWithSharerKey(orderId, buyerId, OrderStatus.DELIVERED);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            ConfirmOrderCommand command = ConfirmOrderCommand.builder()
                    .id(orderId)
                    .buyerId(buyerId)
                    .build();

            confirmOrderService.confirmOrder(command);

            verify(publishOrderEventPort).publishOrderPurchaseConfirmedEvent(
                    eq(orderId), eq(buyerId), anyList()
            );
        }
    }

    // ==================================================================================
    // 구매자 소유권 검증
    // ==================================================================================

    @Nested
    @DisplayName("구매자 소유권 검증")
    class BuyerOwnershipValidationTest {

        @Test
        @DisplayName("타인의 주문을 구매 확정하면 UnauthorizedOrderAccessException이 발생한다")
        void confirmOrder_otherBuyerOrder_throwsException() {
            Long orderId = 1L;
            Long ownerBuyerId = 100L;
            Long attackerBuyerId = 999L;
            Order order = createOrderWithBuyerId(orderId, ownerBuyerId, OrderStatus.DELIVERED);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            ConfirmOrderCommand command = ConfirmOrderCommand.builder()
                    .id(orderId)
                    .buyerId(attackerBuyerId)
                    .build();

            assertThatThrownBy(() -> confirmOrderService.confirmOrder(command))
                    .isInstanceOf(UnauthorizedOrderAccessException.class);
        }

        @Test
        @DisplayName("타인의 주문 구매 확정 시 UpdateOrderPort를 호출하지 않는다")
        void confirmOrder_otherBuyerOrder_doesNotCallUpdatePort() {
            Long orderId = 1L;
            Long ownerBuyerId = 100L;
            Long attackerBuyerId = 999L;
            Order order = createOrderWithBuyerId(orderId, ownerBuyerId, OrderStatus.DELIVERED);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            ConfirmOrderCommand command = ConfirmOrderCommand.builder()
                    .id(orderId)
                    .buyerId(attackerBuyerId)
                    .build();

            assertThatThrownBy(() -> confirmOrderService.confirmOrder(command))
                    .isInstanceOf(UnauthorizedOrderAccessException.class);

            verifyNoInteractions(updateOrderPort);
            verifyNoInteractions(publishOrderEventPort);
        }
    }

    // ==================================================================================
    // 상태 전이 검증
    // ==================================================================================

    @Nested
    @DisplayName("상태 전이 검증")
    class StatusTransitionValidationTest {

        @Test
        @DisplayName("이미 구매 확정된 주문을 다시 구매 확정하면 OrderStatusAlreadyChangedException이 발생한다")
        void confirmOrder_alreadyConfirmed_throwsException() {
            Long orderId = 1L;
            Long buyerId = 100L;
            Order order = createOrderWithBuyerId(orderId, buyerId, OrderStatus.CONFIRMED);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            ConfirmOrderCommand command = ConfirmOrderCommand.builder()
                    .id(orderId)
                    .buyerId(buyerId)
                    .build();

            assertThatThrownBy(() -> confirmOrderService.confirmOrder(command))
                    .isInstanceOf(OrderStatusAlreadyChangedException.class);
        }

        @Test
        @DisplayName("결제 대기 상태의 주문을 구매 확정하면 InvalidOrderStatusTransitionException이 발생한다")
        void confirmOrder_fromPaymentPending_throwsException() {
            Long orderId = 1L;
            Long buyerId = 100L;
            Order order = createOrderWithBuyerId(orderId, buyerId, OrderStatus.PAYMENT_PENDING);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            ConfirmOrderCommand command = ConfirmOrderCommand.builder()
                    .id(orderId)
                    .buyerId(buyerId)
                    .build();

            assertThatThrownBy(() -> confirmOrderService.confirmOrder(command))
                    .isInstanceOf(InvalidOrderStatusTransitionException.class);
        }

        @Test
        @DisplayName("배송중 상태의 주문을 구매 확정하면 InvalidOrderStatusTransitionException이 발생한다")
        void confirmOrder_fromShipping_throwsException() {
            Long orderId = 1L;
            Long buyerId = 100L;
            Order order = createOrderWithBuyerId(orderId, buyerId, OrderStatus.SHIPPING);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            ConfirmOrderCommand command = ConfirmOrderCommand.builder()
                    .id(orderId)
                    .buyerId(buyerId)
                    .build();

            assertThatThrownBy(() -> confirmOrderService.confirmOrder(command))
                    .isInstanceOf(InvalidOrderStatusTransitionException.class);
        }

        @Test
        @DisplayName("취소 상태의 주문을 구매 확정하면 InvalidOrderStatusTransitionException이 발생한다")
        void confirmOrder_fromCancelled_throwsException() {
            Long orderId = 1L;
            Long buyerId = 100L;
            Order order = createOrderWithBuyerId(orderId, buyerId, OrderStatus.CANCELLED);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            ConfirmOrderCommand command = ConfirmOrderCommand.builder()
                    .id(orderId)
                    .buyerId(buyerId)
                    .build();

            assertThatThrownBy(() -> confirmOrderService.confirmOrder(command))
                    .isInstanceOf(InvalidOrderStatusTransitionException.class);
        }

        @Test
        @DisplayName("상태 전이 실패 시 UpdateOrderPort와 PublishOrderEventPort를 호출하지 않는다")
        void confirmOrder_invalidTransition_doesNotCallPorts() {
            Long orderId = 1L;
            Long buyerId = 100L;
            Order order = createOrderWithBuyerId(orderId, buyerId, OrderStatus.SHIPPING);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            ConfirmOrderCommand command = ConfirmOrderCommand.builder()
                    .id(orderId)
                    .buyerId(buyerId)
                    .build();

            assertThatThrownBy(() -> confirmOrderService.confirmOrder(command))
                    .isInstanceOf(InvalidOrderStatusTransitionException.class);

            verifyNoInteractions(updateOrderPort);
            verifyNoInteractions(publishOrderEventPort);
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

    private Order createOrderWithSharerKey(Long orderId, Long buyerId, OrderStatus status) {
        List<OrderProductSnapshotState> productStates = List.of(
                OrderProductSnapshotState.builder()
                        .orderId(orderId)
                        .sellerId(10L)
                        .pricePolicyId(100L)
                        .sharerKey(UUID.randomUUID())
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
