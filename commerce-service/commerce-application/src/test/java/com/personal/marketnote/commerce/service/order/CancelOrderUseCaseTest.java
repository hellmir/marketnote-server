package com.personal.marketnote.commerce.service.order;

import com.personal.marketnote.commerce.domain.order.*;
import com.personal.marketnote.commerce.exception.InvalidOrderStatusTransitionException;
import com.personal.marketnote.commerce.exception.InvalidReasonCategoryException;
import com.personal.marketnote.commerce.exception.OrderStatusAlreadyChangedException;
import com.personal.marketnote.commerce.exception.UnauthorizedOrderAccessException;
import com.personal.marketnote.commerce.port.in.command.order.CancelOrderCommand;
import com.personal.marketnote.commerce.port.in.command.saga.OrderCancelSagaContext;
import com.personal.marketnote.commerce.port.in.usecase.order.GetOrderUseCase;
import com.personal.marketnote.commerce.port.out.event.PublishOrderEventPort;
import com.personal.marketnote.commerce.port.out.fulfillment.CancelFulfillmentReleasePort;
import com.personal.marketnote.commerce.port.out.order.UpdateOrderPort;
import com.personal.marketnote.common.saga.SagaDefinition;
import com.personal.marketnote.common.saga.SagaOrchestrator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
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
    @Mock
    private SagaOrchestrator sagaOrchestrator;
    @Mock
    private SagaDefinition<OrderCancelSagaContext> orderCancelSagaDefinition;

    private CancelOrderService cancelOrderService;

    @BeforeEach
    void setUp() {
        lenient().when(transactionManager.getTransaction(any(TransactionDefinition.class)))
                .thenReturn(mock(TransactionStatus.class));

        cancelOrderService = new CancelOrderService(
                getOrderUseCase,
                updateOrderPort,
                cancelFulfillmentReleasePort,
                publishOrderEventPort,
                transactionManager,
                clock,
                Optional.of(sagaOrchestrator),
                Optional.of(orderCancelSagaDefinition)
        );
    }

    // ==================================================================================
    // 결제 대기 상태 취소 (SAGA 미사용)
    // ==================================================================================

    @Nested
    @DisplayName("결제 대기 상태 취소")
    class PaymentPendingCancelTest {

        @Test
        @DisplayName("결제 대기 상태의 주문을 취소하면 풀필먼트 취소와 SAGA 없이 즉시 CANCELLED 처리된다")
        void cancelOrder_fromPaymentPending_cancelledWithoutSaga() {
            Long orderId = 1L;
            Long buyerId = 100L;
            Order order = createOrder(orderId, buyerId, OrderStatus.PAYMENT_PENDING);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            CancelOrderCommand command = createCommand(orderId, buyerId);

            cancelOrderService.cancelOrder(command);

            verify(updateOrderPort).update(any(Order.class), any(OrderStatusHistory.class));
            verifyNoInteractions(cancelFulfillmentReleasePort);
            verifyNoInteractions(publishOrderEventPort);
            verifyNoInteractions(sagaOrchestrator);
        }
    }

    // ==================================================================================
    // SAGA 모드 — 결제 완료 상태 취소
    // ==================================================================================

    @Nested
    @DisplayName("SAGA 모드 — 결제 완료 상태 취소")
    class SagaModePaidCancelTest {

        @Test
        @DisplayName("결제 완료 상태의 주문을 취소하면 CANCEL_REQUESTED로 변경 후 SAGA를 시작한다")
        void cancelOrder_fromPaid_startsSaga() {
            Long orderId = 1L;
            Long buyerId = 100L;
            Order order = createOrder(orderId, buyerId, OrderStatus.PAID);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            CancelOrderCommand command = createCommand(orderId, buyerId);

            cancelOrderService.cancelOrder(command);

            verify(updateOrderPort).update(any(Order.class), any(OrderStatusHistory.class));
            verify(sagaOrchestrator).start(
                    eq(orderCancelSagaDefinition),
                    eq("ORDER_CANCEL:" + orderId),
                    any(OrderCancelSagaContext.class));
            verifyNoInteractions(cancelFulfillmentReleasePort);
            verifyNoInteractions(publishOrderEventPort);
        }
    }

    // ==================================================================================
    // SAGA 모드 — 상품 준비중 상태 취소
    // ==================================================================================

    @Nested
    @DisplayName("SAGA 모드 — 상품 준비중 상태 취소")
    class SagaModePreparingCancelTest {

        @Test
        @DisplayName("상품 준비중 상태의 주문을 취소하면 CANCEL_REQUESTED로 변경 후 SAGA를 시작한다")
        void cancelOrder_fromPreparing_startsSaga() {
            Long orderId = 1L;
            Long buyerId = 100L;
            Order order = createOrder(orderId, buyerId, OrderStatus.PREPARING);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            CancelOrderCommand command = createCommand(orderId, buyerId);

            cancelOrderService.cancelOrder(command);

            verify(updateOrderPort).update(any(Order.class), any(OrderStatusHistory.class));
            verify(sagaOrchestrator).start(
                    eq(orderCancelSagaDefinition),
                    eq("ORDER_CANCEL:" + orderId),
                    any(OrderCancelSagaContext.class));
            verifyNoInteractions(cancelFulfillmentReleasePort);
            verifyNoInteractions(publishOrderEventPort);
        }
    }

    // ==================================================================================
    // 취소 사유 카테고리 검증
    // ==================================================================================

    @Nested
    @DisplayName("취소 사유 카테고리 검증")
    class CancelReasonCategoryValidationTest {

        @Test
        @DisplayName("반품 전용 사유로 취소 요청하면 InvalidReasonCategoryException이 발생한다")
        void cancelOrder_withReturnOnlyReason_throwsException() {
            Long orderId = 1L;
            Long buyerId = 100L;
            Order order = createOrder(orderId, buyerId, OrderStatus.PAID);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            CancelOrderCommand command = CancelOrderCommand.builder()
                    .id(orderId)
                    .reasonCategory(OrderStatusReasonCategory.PRODUCT_DAMAGE)
                    .reason("상품 파손")
                    .buyerId(buyerId)
                    .build();

            assertThatThrownBy(() -> cancelOrderService.cancelOrder(command))
                    .isInstanceOf(InvalidReasonCategoryException.class);

            verifyNoInteractions(updateOrderPort);
            verifyNoInteractions(sagaOrchestrator);
        }

        @Test
        @DisplayName("취소 전용 사유로 취소 요청하면 정상 처리된다")
        void cancelOrder_withCancelReason_succeeds() {
            Long orderId = 1L;
            Long buyerId = 100L;
            Order order = createOrder(orderId, buyerId, OrderStatus.PAYMENT_PENDING);
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
        @DisplayName("공용 사유(MISTAKE)로 취소 요청하면 정상 처리된다")
        void cancelOrder_withBothReason_succeeds() {
            Long orderId = 1L;
            Long buyerId = 100L;
            Order order = createOrder(orderId, buyerId, OrderStatus.PAYMENT_PENDING);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            CancelOrderCommand command = CancelOrderCommand.builder()
                    .id(orderId)
                    .reasonCategory(OrderStatusReasonCategory.MISTAKE)
                    .reason("주문 실수")
                    .buyerId(buyerId)
                    .build();

            assertThatCode(() -> cancelOrderService.cancelOrder(command))
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
            verifyNoInteractions(sagaOrchestrator);
        }
    }

    // ==================================================================================
    // 상태 전이 검증
    // ==================================================================================

    @Nested
    @DisplayName("상태 전이 검증")
    class StatusTransitionValidationTest {

        @Test
        @DisplayName("이미 취소된 주문을 다시 취소 요청하면 InvalidOrderStatusTransitionException이 발생한다")
        void cancelOrder_alreadyCancelled_throwsInvalidTransitionException() {
            Long orderId = 1L;
            Long buyerId = 100L;
            Order order = createOrder(orderId, buyerId, OrderStatus.CANCELLED);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            CancelOrderCommand command = createCommand(orderId, buyerId);

            assertThatThrownBy(() -> cancelOrderService.cancelOrder(command))
                    .isInstanceOf(InvalidOrderStatusTransitionException.class);

            verifyNoInteractions(updateOrderPort);
            verifyNoInteractions(sagaOrchestrator);
        }

        @Test
        @DisplayName("이미 취소 요청된 주문을 다시 취소 요청하면 OrderStatusAlreadyChangedException이 발생한다")
        void cancelOrder_alreadyCancelRequested_throwsAlreadyChangedException() {
            Long orderId = 1L;
            Long buyerId = 100L;
            Order order = createOrder(orderId, buyerId, OrderStatus.CANCEL_REQUESTED);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            CancelOrderCommand command = createCommand(orderId, buyerId);

            assertThatThrownBy(() -> cancelOrderService.cancelOrder(command))
                    .isInstanceOf(OrderStatusAlreadyChangedException.class);

            verifyNoInteractions(updateOrderPort);
            verifyNoInteractions(sagaOrchestrator);
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
            verifyNoInteractions(sagaOrchestrator);
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
