package com.personal.marketnote.commerce.service.order;

import com.personal.marketnote.commerce.domain.order.InvalidOrderStatusTransitionException;
import com.personal.marketnote.commerce.domain.order.Order;
import com.personal.marketnote.commerce.domain.order.OrderProduct;
import com.personal.marketnote.commerce.domain.order.OrderStatus;
import com.personal.marketnote.commerce.domain.order.OrderStatusHistory;
import com.personal.marketnote.commerce.port.in.command.order.CompleteCancelOrderCommand;
import com.personal.marketnote.commerce.port.in.command.order.CompleteCancelOrderCommand.CancelProductItemCommand;
import com.personal.marketnote.commerce.port.in.usecase.order.GetOrderUseCase;
import com.personal.marketnote.commerce.port.out.event.PublishOrderEventPort;
import com.personal.marketnote.commerce.port.out.order.UpdateOrderPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CompleteCancelOrderUseCase 테스트")
class CompleteCancelOrderUseCaseTest {

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
                    any(), any(), isNull());
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

    @SuppressWarnings("unchecked")
    @Nested
    @DisplayName("취소 상품 목록 전달 검증")
    class CancelProductsVerification {

        @Test
        @DisplayName("전체 취소 시 cancelProducts에 전체 상품이 전달된다")
        void shouldPassAllProductsAsCancelProductsWhenFullCancel() {
            // given
            Order order = mock(Order.class);
            when(order.getOrderStatus()).thenReturn(OrderStatus.CANCEL_REQUESTED);
            when(order.getOrderProducts()).thenReturn(List.of());
            when(getOrderUseCase.getOrder(1L)).thenReturn(order);

            UUID sharerKey1 = UUID.fromString("11111111-1111-1111-1111-111111111111");
            UUID sharerKey2 = UUID.fromString("22222222-2222-2222-2222-222222222222");
            CancelProductItemCommand item1 = new CancelProductItemCommand(10L, sharerKey1, 2, 25000L);
            CancelProductItemCommand item2 = new CancelProductItemCommand(20L, sharerKey2, 1, 15000L);

            CompleteCancelOrderCommand command = CompleteCancelOrderCommand.builder()
                    .orderId(1L)
                    .orderKey("ORD-001")
                    .buyerId(100L)
                    .cancelAmount(65000L)
                    .paymentAmount(65000L)
                    .pointAmount(0L)
                    .shippingFee(3000L)
                    .isFullCancel(true)
                    .alreadyRefunded(0L)
                    .cancelProducts(List.of(item1, item2))
                    .build();

            // when
            service.completeCancellation(command);

            // then
            ArgumentCaptor<List<OrderProduct>> cancelProductsCaptor = ArgumentCaptor.forClass(List.class);
            verify(publishOrderEventPort).publishOrderCancelledEvent(
                    eq(1L), eq("ORD-001"), eq(100L),
                    eq(65000L), eq(65000L), eq(0L), eq(3000L),
                    eq(true), eq(0L),
                    any(), cancelProductsCaptor.capture(), isNull()
            );

            List<OrderProduct> capturedProducts = cancelProductsCaptor.getValue();
            assertThat(capturedProducts).hasSize(2);
            assertThat(capturedProducts).extracting(OrderProduct::getPricePolicyId)
                    .containsExactlyInAnyOrder(10L, 20L);
        }

        @Test
        @DisplayName("부분 취소 시 cancelProducts에 취소 대상 상품만 전달된다")
        void shouldPassOnlyCancelTargetProductsWhenPartialCancel() {
            // given
            Order order = mock(Order.class);
            when(order.getOrderStatus()).thenReturn(OrderStatus.CANCEL_REQUESTED);

            OrderProduct existingProduct1 = mock(OrderProduct.class);
            OrderProduct existingProduct2 = mock(OrderProduct.class);
            when(order.getOrderProducts()).thenReturn(List.of(existingProduct1, existingProduct2));
            when(getOrderUseCase.getOrder(1L)).thenReturn(order);

            UUID sharerKey = UUID.fromString("11111111-1111-1111-1111-111111111111");
            CancelProductItemCommand cancelItem = new CancelProductItemCommand(10L, sharerKey, 2, 25000L);

            CompleteCancelOrderCommand command = CompleteCancelOrderCommand.builder()
                    .orderId(1L)
                    .orderKey("ORD-001")
                    .buyerId(100L)
                    .cancelAmount(50000L)
                    .paymentAmount(65000L)
                    .pointAmount(0L)
                    .shippingFee(3000L)
                    .isFullCancel(false)
                    .alreadyRefunded(0L)
                    .cancelProducts(List.of(cancelItem))
                    .build();

            // when
            service.completeCancellation(command);

            // then
            ArgumentCaptor<List<OrderProduct>> cancelProductsCaptor = ArgumentCaptor.forClass(List.class);
            verify(publishOrderEventPort).publishOrderCancelledEvent(
                    eq(1L), eq("ORD-001"), eq(100L),
                    eq(50000L), eq(65000L), eq(0L), eq(3000L),
                    eq(false), eq(0L),
                    any(), cancelProductsCaptor.capture(), isNull()
            );

            List<OrderProduct> capturedProducts = cancelProductsCaptor.getValue();
            assertThat(capturedProducts).hasSize(1);
            assertThat(capturedProducts.get(0).getPricePolicyId()).isEqualTo(10L);
        }

        @Test
        @DisplayName("재고 복구 시 cancelProducts에 포함된 상품만 복구된다")
        void shouldMapCancelProductItemsCorrectlyForStockRecovery() {
            // given
            Order order = mock(Order.class);
            when(order.getOrderStatus()).thenReturn(OrderStatus.CANCEL_REQUESTED);
            when(order.getOrderProducts()).thenReturn(List.of());
            when(getOrderUseCase.getOrder(1L)).thenReturn(order);

            UUID sharerKey = UUID.fromString("33333333-3333-3333-3333-333333333333");
            CancelProductItemCommand cancelItem = new CancelProductItemCommand(30L, sharerKey, 3, 20000L);

            CompleteCancelOrderCommand command = CompleteCancelOrderCommand.builder()
                    .orderId(1L)
                    .orderKey("ORD-001")
                    .buyerId(100L)
                    .cancelAmount(60000L)
                    .paymentAmount(60000L)
                    .pointAmount(0L)
                    .shippingFee(3000L)
                    .isFullCancel(true)
                    .alreadyRefunded(0L)
                    .cancelProducts(List.of(cancelItem))
                    .build();

            // when
            service.completeCancellation(command);

            // then
            ArgumentCaptor<List<OrderProduct>> cancelProductsCaptor = ArgumentCaptor.forClass(List.class);
            verify(publishOrderEventPort).publishOrderCancelledEvent(
                    any(), any(), any(), any(), any(), any(), any(),
                    anyBoolean(), any(),
                    any(), cancelProductsCaptor.capture(), any()
            );

            List<OrderProduct> capturedProducts = cancelProductsCaptor.getValue();
            assertThat(capturedProducts).hasSize(1);
            OrderProduct cancelProduct = capturedProducts.get(0);
            assertThat(cancelProduct.getPricePolicyId()).isEqualTo(30L);
            assertThat(cancelProduct.getSharerKey()).isEqualTo(sharerKey);
            assertThat(cancelProduct.getQuantity().getValue()).isEqualTo(3);
            assertThat(cancelProduct.getUnitAmount().getValue()).isEqualTo(20000L);
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
