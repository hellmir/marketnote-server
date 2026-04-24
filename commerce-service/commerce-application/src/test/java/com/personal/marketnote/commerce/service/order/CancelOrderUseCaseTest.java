package com.personal.marketnote.commerce.service.order;

import com.personal.marketnote.commerce.domain.order.*;
import com.personal.marketnote.commerce.exception.InvalidOrderStatusTransitionException;
import com.personal.marketnote.commerce.exception.OrderCancellationNotAllowedException;
import com.personal.marketnote.commerce.exception.OrderStatusAlreadyChangedException;
import com.personal.marketnote.commerce.exception.UnauthorizedOrderAccessException;
import com.personal.marketnote.commerce.port.in.command.order.CancelOrderCommand;
import com.personal.marketnote.commerce.port.in.usecase.order.GetOrderUseCase;
import com.personal.marketnote.commerce.port.out.event.PublishOrderEventPort;
import com.personal.marketnote.commerce.port.out.fulfillment.CancelFulfillmentReleasePort;
import com.personal.marketnote.commerce.port.out.fulfillment.CancelFulfillmentReleaseResult;
import com.personal.marketnote.commerce.port.out.order.UpdateOrderPort;
import com.personal.marketnote.common.exception.FulfillmentServiceRequestFailedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;

import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CancelOrderUseCaseTest {
    @Mock
    private GetOrderUseCase getOrderUseCase;
    @Mock
    private UpdateOrderPort updateOrderPort;
    @Mock
    private CancelFulfillmentReleasePort cancelFulfillmentReleasePort;
    @Mock
    private PublishOrderEventPort publishOrderEventPort;
    @Mock
    private PlatformTransactionManager transactionManager;
    @Spy
    private Clock clock = Clock.fixed(Instant.parse("2026-04-05T00:00:00Z"), ZoneId.of("Asia/Seoul"));

    @InjectMocks
    private CancelOrderService cancelOrderService;

    @BeforeEach
    void setUp() {
        lenient().when(transactionManager.getTransaction(any(TransactionDefinition.class)))
                .thenReturn(mock(TransactionStatus.class));
    }

    // ==================================================================================
    // 결제 대기 상태 취소
    // ==================================================================================

    @Nested
    @DisplayName("결제 대기 상태 취소")
    class PaymentPendingCancelTest {

        @Test
        @DisplayName("결제 대기 상태의 주문을 취소하면 풀필먼트 취소 없이 즉시 CANCELLED 처리된다")
        void cancelOrder_fromPaymentPending_cancelledWithoutFulfillmentAndEvent() {
            Long orderId = 1L;
            Long buyerId = 100L;
            Order order = createOrder(orderId, buyerId, OrderStatus.PAYMENT_PENDING);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            CancelOrderCommand command = createCommand(orderId, buyerId);

            cancelOrderService.cancelOrder(command);

            verify(updateOrderPort).update(any(Order.class), any(OrderStatusHistory.class));
            verifyNoInteractions(cancelFulfillmentReleasePort);
            verifyNoInteractions(publishOrderEventPort);
        }
    }

    // ==================================================================================
    // 결제 완료 상태 취소
    // ==================================================================================

    @Nested
    @DisplayName("결제 완료 상태 취소")
    class PaidCancelTest {

        @Test
        @DisplayName("결제 완료 상태의 주문을 취소하면 풀필먼트 취소 없이 이벤트를 발행한다")
        void cancelOrder_fromPaid_publishesEventWithoutFulfillmentCancel() {
            Long orderId = 1L;
            Long buyerId = 100L;
            Order order = createOrder(orderId, buyerId, OrderStatus.PAID);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            CancelOrderCommand command = createCommand(orderId, buyerId);

            cancelOrderService.cancelOrder(command);

            verify(updateOrderPort).update(any(Order.class), any(OrderStatusHistory.class));
            verifyNoInteractions(cancelFulfillmentReleasePort);
            verify(publishOrderEventPort).publishOrderCancelledEvent(
                    eq(orderId), any(String.class), eq(buyerId),
                    eq(50000L), eq(50000L), eq(0L),
                    eq(3000L), eq(true), eq(0L),
                    any(), any()
            );
        }
    }

    // ==================================================================================
    // 상품 준비중 상태 취소 — 풀필먼트 취소 성공
    // ==================================================================================

    @Nested
    @DisplayName("상품 준비중 상태 취소 — 풀필먼트 취소 성공")
    class PreparingCancelSuccessTest {

        @Test
        @DisplayName("상품 준비중 상태에서 풀필먼트 취소 성공 시 CANCELLED 처리되고 이벤트를 발행한다")
        void cancelOrder_fromPreparing_fulfillmentApproved_cancelledAndPublishesEvent() {
            Long orderId = 1L;
            Long buyerId = 100L;
            Order order = createOrder(orderId, buyerId, OrderStatus.PREPARING);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);
            when(cancelFulfillmentReleasePort.cancelRelease(orderId))
                    .thenReturn(new CancelFulfillmentReleaseResult(orderId, true, "취소 성공"));

            CancelOrderCommand command = createCommand(orderId, buyerId);

            cancelOrderService.cancelOrder(command);

            verify(cancelFulfillmentReleasePort).cancelRelease(orderId);
            verify(updateOrderPort).update(any(Order.class), any(OrderStatusHistory.class));
            verify(publishOrderEventPort).publishOrderCancelledEvent(
                    eq(orderId), any(String.class), eq(buyerId),
                    eq(50000L), eq(50000L), eq(0L),
                    eq(3000L), eq(true), eq(0L),
                    any(), any()
            );
        }
    }

    // ==================================================================================
    // 풀필먼트 취소 실패
    // ==================================================================================

    @Nested
    @DisplayName("풀필먼트 취소 실패")
    class FulfillmentCancelFailureTest {

        @Test
        @DisplayName("풀필먼트가 출고 취소를 거부하면 OrderCancellationNotAllowedException이 발생한다")
        void cancelOrder_fulfillmentRejected_throwsException() {
            Long orderId = 1L;
            Long buyerId = 100L;
            Order order = createOrder(orderId, buyerId, OrderStatus.PREPARING);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);
            when(cancelFulfillmentReleasePort.cancelRelease(orderId))
                    .thenReturn(new CancelFulfillmentReleaseResult(orderId, false, "피킹 완료"));

            CancelOrderCommand command = createCommand(orderId, buyerId);

            assertThatThrownBy(() -> cancelOrderService.cancelOrder(command))
                    .isInstanceOf(OrderCancellationNotAllowedException.class)
                    .hasMessageContaining("피킹완료 이후에는 주문 취소가 불가능합니다. 반품으로 처리해 주세요.");

            verifyNoInteractions(updateOrderPort);
            verifyNoInteractions(publishOrderEventPort);
        }

        @Test
        @DisplayName("풀필먼트 서비스 통신 실패 시 OrderCancellationNotAllowedException이 발생한다")
        void cancelOrder_fulfillmentCommunicationFailure_throwsException() {
            Long orderId = 1L;
            Long buyerId = 100L;
            Order order = createOrder(orderId, buyerId, OrderStatus.PREPARING);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);
            when(cancelFulfillmentReleasePort.cancelRelease(orderId))
                    .thenThrow(new FulfillmentServiceRequestFailedException(new IOException("connection refused")));

            CancelOrderCommand command = createCommand(orderId, buyerId);

            assertThatThrownBy(() -> cancelOrderService.cancelOrder(command))
                    .isInstanceOf(OrderCancellationNotAllowedException.class)
                    .hasMessageContaining("피킹완료 이후에는 주문 취소가 불가능합니다. 반품으로 처리해 주세요.");

            verifyNoInteractions(updateOrderPort);
            verifyNoInteractions(publishOrderEventPort);
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
            Order order = createOrder(orderId, ownerBuyerId, OrderStatus.PAID);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            CancelOrderCommand command = createCommand(orderId, attackerBuyerId);

            assertThatThrownBy(() -> cancelOrderService.cancelOrder(command))
                    .isInstanceOf(UnauthorizedOrderAccessException.class);

            verifyNoInteractions(updateOrderPort);
            verifyNoInteractions(cancelFulfillmentReleasePort);
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
        @DisplayName("이미 취소된 주문을 다시 취소 요청하면 OrderStatusAlreadyChangedException이 발생한다")
        void cancelOrder_alreadyCancelled_throwsAlreadyChangedException() {
            Long orderId = 1L;
            Long buyerId = 100L;
            Order order = createOrder(orderId, buyerId, OrderStatus.CANCELLED);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            CancelOrderCommand command = createCommand(orderId, buyerId);

            assertThatThrownBy(() -> cancelOrderService.cancelOrder(command))
                    .isInstanceOf(OrderStatusAlreadyChangedException.class);

            verifyNoInteractions(updateOrderPort);
            verifyNoInteractions(cancelFulfillmentReleasePort);
            verifyNoInteractions(publishOrderEventPort);
        }

        @Test
        @DisplayName("배송중 상태의 주문을 취소 요청하면 InvalidOrderStatusTransitionException이 발생한다")
        void cancelOrder_fromShipping_throwsException() {
            Long orderId = 1L;
            Long buyerId = 100L;
            Order order = createOrder(orderId, buyerId, OrderStatus.SHIPPING);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            CancelOrderCommand command = createCommand(orderId, buyerId);

            assertThatThrownBy(() -> cancelOrderService.cancelOrder(command))
                    .isInstanceOf(InvalidOrderStatusTransitionException.class);

            verifyNoInteractions(updateOrderPort);
            verifyNoInteractions(cancelFulfillmentReleasePort);
            verifyNoInteractions(publishOrderEventPort);
        }
    }

    // ==================================================================================
    // 헬퍼 메서드
    // ==================================================================================

    private CancelOrderCommand createCommand(Long orderId, Long buyerId) {
        return CancelOrderCommand.builder()
                .id(orderId)
                .reasonCategory(OrderStatusReasonCategory.CANCEL_ORDER)
                .reason("구매 의사 취소")
                .buyerId(buyerId)
                .build();
    }

    private Order createOrder(Long orderId, Long buyerId, OrderStatus status) {
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
                .amount(OrderAmount.of(50000L, 50000L, 0L, 0L, 3000L))
                .shippingAddress(ShippingAddress.of("수령인", "01012345678", "12345", "서울시 강남구", "상세주소", null, null))
                .orderProductStates(productStates)
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build());
    }
}
