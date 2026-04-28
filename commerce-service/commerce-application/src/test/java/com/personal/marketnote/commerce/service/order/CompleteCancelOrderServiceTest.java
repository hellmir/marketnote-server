package com.personal.marketnote.commerce.service.order;

import com.personal.marketnote.commerce.domain.order.Order;
import com.personal.marketnote.commerce.domain.order.OrderStatus;
import com.personal.marketnote.commerce.domain.order.OrderStatusHistory;
import com.personal.marketnote.commerce.exception.InvalidOrderStatusTransitionException;
import com.personal.marketnote.commerce.port.in.command.order.CompleteCancelOrderCommand;
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
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CompleteCancelOrderService 테스트")
class CompleteCancelOrderServiceTest {

    @InjectMocks
    private CompleteCancelOrderService service;

    @Mock
    private GetOrderUseCase getOrderUseCase;

    @Mock
    private UpdateOrderPort updateOrderPort;

    @Mock
    private PublishOrderEventPort publishOrderEventPort;

    @Spy
    private Clock clock = Clock.fixed(Instant.parse("2026-04-26T10:00:00Z"), ZoneId.of("Asia/Seoul"));

    @Nested
    @DisplayName("주문 취소 완료 처리 성공")
    class CompleteCancellationSuccess {

        @Test
        @DisplayName("CANCEL_REQUESTED 상태의 주문을 CANCELLED로 전이하고 이벤트를 발행한다")
        void shouldTransitionToCancelledAndPublishEvent() {
            Order order = mock(Order.class);
            when(order.getOrderStatus()).thenReturn(OrderStatus.CANCEL_REQUESTED);
            when(order.getOrderProducts()).thenReturn(List.of());
            when(getOrderUseCase.getOrder(1L)).thenReturn(order);

            CompleteCancelOrderCommand command = CompleteCancelOrderCommand.builder()
                    .orderId(1L)
                    .orderKey("ORD-001")
                    .buyerId(100L)
                    .cancelAmount(50000L)
                    .paymentAmount(50000L)
                    .pointAmount(0L)
                    .shippingFee(3000L)
                    .isFullCancel(true)
                    .alreadyRefunded(0L)
                    .reasonCategory("CANCEL_ORDER")
                    .reason("구매 의사 취소")
                    .build();

            service.completeCancellation(command);

            verify(order).changeAllProductsStatus(eq(OrderStatus.CANCELLED), any());
            verify(updateOrderPort).update(eq(order), any(OrderStatusHistory.class));
            verify(publishOrderEventPort).publishOrderCancelledEvent(
                    eq(1L), eq("ORD-001"), eq(100L),
                    eq(50000L), eq(50000L), eq(0L), eq(3000L),
                    eq(true), eq(0L),
                    any(), any());
        }

        @Test
        @DisplayName("reasonCategory가 null이면 null로 전달한다")
        void shouldPassNullReasonCategory() {
            Order order = mock(Order.class);
            when(order.getOrderStatus()).thenReturn(OrderStatus.CANCEL_REQUESTED);
            when(order.getOrderProducts()).thenReturn(List.of());
            when(getOrderUseCase.getOrder(1L)).thenReturn(order);

            CompleteCancelOrderCommand command = CompleteCancelOrderCommand.builder()
                    .orderId(1L)
                    .orderKey("ORD-001")
                    .buyerId(100L)
                    .cancelAmount(50000L)
                    .paymentAmount(50000L)
                    .pointAmount(0L)
                    .shippingFee(3000L)
                    .isFullCancel(true)
                    .alreadyRefunded(0L)
                    .reasonCategory(null)
                    .reason(null)
                    .build();

            service.completeCancellation(command);

            verify(updateOrderPort).update(eq(order), any(OrderStatusHistory.class));
        }
    }

    @Nested
    @DisplayName("주문 취소 완료 처리 실패")
    class CompleteCancellationFailure {

        @Test
        @DisplayName("CANCEL_REQUESTED가 아닌 상태이면 InvalidOrderStatusTransitionException을 던진다")
        void shouldThrowOnNonCancelRequestedStatus() {
            Order order = mock(Order.class);
            when(order.getOrderStatus()).thenReturn(OrderStatus.PAID);
            when(getOrderUseCase.getOrder(1L)).thenReturn(order);

            CompleteCancelOrderCommand command = CompleteCancelOrderCommand.builder()
                    .orderId(1L)
                    .orderKey("ORD-001")
                    .buyerId(100L)
                    .cancelAmount(50000L)
                    .paymentAmount(50000L)
                    .pointAmount(0L)
                    .shippingFee(3000L)
                    .isFullCancel(true)
                    .alreadyRefunded(0L)
                    .build();

            assertThatThrownBy(() -> service.completeCancellation(command))
                    .isInstanceOf(InvalidOrderStatusTransitionException.class);

            verifyNoInteractions(updateOrderPort);
            verifyNoInteractions(publishOrderEventPort);
        }
    }
}
