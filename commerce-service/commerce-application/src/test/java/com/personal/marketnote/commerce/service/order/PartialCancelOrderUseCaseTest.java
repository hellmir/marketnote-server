package com.personal.marketnote.commerce.service.order;

import com.personal.marketnote.commerce.domain.order.*;
import com.personal.marketnote.commerce.exception.*;
import com.personal.marketnote.commerce.port.in.command.order.CancelOrderCommand;
import com.personal.marketnote.commerce.port.in.command.order.PartialCancelOrderCommand;
import com.personal.marketnote.commerce.port.in.command.saga.OrderCancelSagaContext;
import com.personal.marketnote.commerce.port.in.usecase.order.CancelOrderUseCase;
import com.personal.marketnote.commerce.port.in.usecase.order.GetOrderUseCase;
import com.personal.marketnote.commerce.port.out.event.PublishOrderEventPort;
import com.personal.marketnote.commerce.port.out.fulfillment.CancelFulfillmentReleasePort;
import com.personal.marketnote.commerce.port.out.fulfillment.CancelFulfillmentReleaseResult;
import com.personal.marketnote.commerce.port.out.order.UpdateOrderPort;
import com.personal.marketnote.commerce.service.saga.OrderCancelSagaStarter;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PartialCancelOrderUseCaseTest {
    @Mock
    private GetOrderUseCase getOrderUseCase;
    @Mock
    private CancelOrderUseCase cancelOrderUseCase;
    @Mock
    private UpdateOrderPort updateOrderPort;
    @Mock
    private CancelFulfillmentReleasePort cancelFulfillmentReleasePort;
    @Mock
    private PublishOrderEventPort publishOrderEventPort;
    @Mock
    private PlatformTransactionManager transactionManager;
    @Spy
    private Clock clock = Clock.fixed(Instant.parse("2026-10-11T00:00:00Z"), ZoneId.of("Asia/Seoul"));
    @Mock
    private OrderCancelSagaStarter orderCancelSagaStarter;

    private PartialCancelOrderService partialCancelOrderService;

    @BeforeEach
    void setUp() {
        lenient().when(transactionManager.getTransaction(any(TransactionDefinition.class)))
                .thenReturn(mock(TransactionStatus.class));

        partialCancelOrderService = new PartialCancelOrderService(
                getOrderUseCase,
                cancelOrderUseCase,
                updateOrderPort,
                cancelFulfillmentReleasePort,
                publishOrderEventPort,
                transactionManager,
                clock,
                Optional.of(orderCancelSagaStarter)
        );
    }

    // ==================================================================================
    // 정상 부분 취소 — SAGA 모드
    // ==================================================================================

    @Nested
    @DisplayName("정상 부분 취소 — SAGA 모드")
    class SuccessfulPartialCancelSagaModeTest {

        @Test
        @DisplayName("결제 완료 상태에서 일부 상품을 취소하면 CANCEL_REQUESTED로 변경 후 SAGA Starter를 호출한다")
        void partialCancelOrder_fromPaid_startsSaga() {
            Long orderId = 1L;
            Long buyerId = 100L;
            Order order = createOrderWithMultipleProducts(orderId, buyerId, OrderStatus.PAID);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            PartialCancelOrderCommand command = createPartialCancelCommand(orderId, buyerId, List.of(100L));

            partialCancelOrderService.partialCancelOrder(command);

            verify(updateOrderPort).update(any(Order.class), any(OrderStatusHistory.class));
            verify(orderCancelSagaStarter).start(any(OrderCancelSagaContext.class));
            verifyNoInteractions(cancelFulfillmentReleasePort);
        }

        @Test
        @DisplayName("상품 준비중 상태에서 일부 상품을 취소하면 CANCEL_REQUESTED로 변경 후 SAGA Starter를 호출한다")
        void partialCancelOrder_fromPreparing_startsSaga() {
            Long orderId = 1L;
            Long buyerId = 100L;
            Order order = createOrderWithMultipleProducts(orderId, buyerId, OrderStatus.PREPARING);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            PartialCancelOrderCommand command = createPartialCancelCommand(orderId, buyerId, List.of(100L));

            partialCancelOrderService.partialCancelOrder(command);

            verify(updateOrderPort).update(any(Order.class), any(OrderStatusHistory.class));
            verify(orderCancelSagaStarter).start(any(OrderCancelSagaContext.class));
        }
    }

    // ==================================================================================
    // 전체 상품 취소 시 전체 취소로 위임
    // ==================================================================================

    @Nested
    @DisplayName("전체 상품 취소 시 전체 취소로 위임")
    class DelegateToFullCancelTest {

        @Test
        @DisplayName("모든 미취소 상품이 취소 대상이면 CancelOrderUseCase로 위임한다")
        void partialCancelOrder_allProductsCancelled_delegatesToFullCancel() {
            Long orderId = 1L;
            Long buyerId = 100L;
            Order order = createOrderWithMultipleProducts(orderId, buyerId, OrderStatus.PAID);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            PartialCancelOrderCommand command = createPartialCancelCommand(orderId, buyerId, List.of(100L, 200L));

            partialCancelOrderService.partialCancelOrder(command);

            verify(cancelOrderUseCase).cancelOrder(any(CancelOrderCommand.class));
            verifyNoInteractions(updateOrderPort);
            verifyNoInteractions(orderCancelSagaStarter);
        }
    }

    // ==================================================================================
    // 부분 취소 상태에서 추가 부분 취소
    // ==================================================================================

    @Nested
    @DisplayName("부분 취소 상태에서 추가 부분 취소")
    class AdditionalPartialCancelTest {

        @Test
        @DisplayName("PARTIALLY_CANCELLED 상태에서 남은 일부 상품을 추가 부분 취소할 수 있다")
        void partialCancelOrder_fromPartiallyCancelled_additionalPartialCancel() {
            Long orderId = 1L;
            Long buyerId = 100L;
            Order order = createOrderWithPartiallyCancelledProducts(orderId, buyerId);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            PartialCancelOrderCommand command = createPartialCancelCommand(orderId, buyerId, List.of(200L));

            partialCancelOrderService.partialCancelOrder(command);

            verify(updateOrderPort).update(any(Order.class), any(OrderStatusHistory.class));
            verify(orderCancelSagaStarter).start(any(OrderCancelSagaContext.class));
        }
    }

    // ==================================================================================
    // 구매자 소유권 검증
    // ==================================================================================

    @Nested
    @DisplayName("구매자 소유권 검증")
    class BuyerOwnershipValidationTest {

        @Test
        @DisplayName("타인의 주문을 부분 취소 요청하면 UnauthorizedOrderAccessException이 발생한다")
        void partialCancelOrder_otherBuyerOrder_throwsException() {
            Long orderId = 1L;
            Long ownerBuyerId = 100L;
            Long attackerBuyerId = 999L;
            Order order = createOrderWithMultipleProducts(orderId, ownerBuyerId, OrderStatus.PAID);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            PartialCancelOrderCommand command = createPartialCancelCommand(orderId, attackerBuyerId, List.of(100L));

            assertThatThrownBy(() -> partialCancelOrderService.partialCancelOrder(command))
                    .isInstanceOf(UnauthorizedOrderAccessException.class);

            verifyNoInteractions(updateOrderPort);
            verifyNoInteractions(orderCancelSagaStarter);
        }
    }

    // ==================================================================================
    // 취소 사유 카테고리 검증
    // ==================================================================================

    @Nested
    @DisplayName("취소 사유 카테고리 검증")
    class ReasonCategoryValidationTest {

        @Test
        @DisplayName("반품 전용 사유로 부분 취소 요청하면 InvalidReasonCategoryException이 발생한다")
        void partialCancelOrder_withReturnOnlyReason_throwsException() {
            Long orderId = 1L;
            Long buyerId = 100L;
            Order order = createOrderWithMultipleProducts(orderId, buyerId, OrderStatus.PAID);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            PartialCancelOrderCommand command = PartialCancelOrderCommand.builder()
                    .id(orderId)
                    .reasonCategory(OrderStatusReasonCategory.PRODUCT_DAMAGE)
                    .reason("상품 파손")
                    .buyerId(buyerId)
                    .cancelPricePolicyIds(List.of(100L))
                    .build();

            assertThatThrownBy(() -> partialCancelOrderService.partialCancelOrder(command))
                    .isInstanceOf(InvalidReasonCategoryException.class);

            verifyNoInteractions(updateOrderPort);
            verifyNoInteractions(orderCancelSagaStarter);
        }
    }

    // ==================================================================================
    // 상태 전이 검증 — 부분 취소 불가 상태
    // ==================================================================================

    @Nested
    @DisplayName("상태 전이 검증 — 부분 취소 불가 상태")
    class StatusTransitionValidationTest {

        @Test
        @DisplayName("배송중 상태의 주문을 부분 취소 요청하면 PartialCancelNotAllowedException이 발생한다")
        void partialCancelOrder_fromShipping_throwsException() {
            Long orderId = 1L;
            Long buyerId = 100L;
            Order order = createOrderWithMultipleProducts(orderId, buyerId, OrderStatus.SHIPPING);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            PartialCancelOrderCommand command = createPartialCancelCommand(orderId, buyerId, List.of(100L));

            assertThatThrownBy(() -> partialCancelOrderService.partialCancelOrder(command))
                    .isInstanceOf(PartialCancelNotAllowedException.class);

            verifyNoInteractions(updateOrderPort);
            verifyNoInteractions(orderCancelSagaStarter);
        }

        @Test
        @DisplayName("배송 완료 상태의 주문을 부분 취소 요청하면 PartialCancelNotAllowedException이 발생한다")
        void partialCancelOrder_fromDelivered_throwsException() {
            Long orderId = 1L;
            Long buyerId = 100L;
            Order order = createOrderWithMultipleProducts(orderId, buyerId, OrderStatus.DELIVERED);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            PartialCancelOrderCommand command = createPartialCancelCommand(orderId, buyerId, List.of(100L));

            assertThatThrownBy(() -> partialCancelOrderService.partialCancelOrder(command))
                    .isInstanceOf(PartialCancelNotAllowedException.class);

            verifyNoInteractions(updateOrderPort);
            verifyNoInteractions(orderCancelSagaStarter);
        }

        @Test
        @DisplayName("이미 전체 취소된 주문을 부분 취소 요청하면 PartialCancelNotAllowedException이 발생한다")
        void partialCancelOrder_fromCancelled_throwsException() {
            Long orderId = 1L;
            Long buyerId = 100L;
            Order order = createOrderWithMultipleProducts(orderId, buyerId, OrderStatus.CANCELLED);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            PartialCancelOrderCommand command = createPartialCancelCommand(orderId, buyerId, List.of(100L));

            assertThatThrownBy(() -> partialCancelOrderService.partialCancelOrder(command))
                    .isInstanceOf(PartialCancelNotAllowedException.class);

            verifyNoInteractions(updateOrderPort);
            verifyNoInteractions(orderCancelSagaStarter);
        }

        @Test
        @DisplayName("취소 요청 진행 중인 주문을 부분 취소 요청하면 PartialCancelNotAllowedException이 발생한다")
        void partialCancelOrder_fromCancelRequested_throwsException() {
            Long orderId = 1L;
            Long buyerId = 100L;
            Order order = createOrderWithMultipleProducts(orderId, buyerId, OrderStatus.CANCEL_REQUESTED);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            PartialCancelOrderCommand command = createPartialCancelCommand(orderId, buyerId, List.of(100L));

            assertThatThrownBy(() -> partialCancelOrderService.partialCancelOrder(command))
                    .isInstanceOf(PartialCancelNotAllowedException.class);

            verifyNoInteractions(updateOrderPort);
            verifyNoInteractions(orderCancelSagaStarter);
        }
    }

    // ==================================================================================
    // 취소 대상 상품 유효성 검증
    // ==================================================================================

    @Nested
    @DisplayName("취소 대상 상품 유효성 검증")
    class CancelProductValidationTest {

        @Test
        @DisplayName("빈 취소 대상 목록으로 부분 취소 요청하면 InvalidPartialCancelProductException이 발생한다")
        void partialCancelOrder_emptyCancelProducts_throwsException() {
            Long orderId = 1L;
            Long buyerId = 100L;
            Order order = createOrderWithMultipleProducts(orderId, buyerId, OrderStatus.PAID);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            PartialCancelOrderCommand command = createPartialCancelCommand(orderId, buyerId, List.of());

            assertThatThrownBy(() -> partialCancelOrderService.partialCancelOrder(command))
                    .isInstanceOf(InvalidPartialCancelProductException.class);

            verifyNoInteractions(updateOrderPort);
            verifyNoInteractions(orderCancelSagaStarter);
        }

        @Test
        @DisplayName("주문에 존재하지 않는 pricePolicyId로 부분 취소 요청하면 InvalidPartialCancelProductException이 발생한다")
        void partialCancelOrder_nonExistentProduct_throwsException() {
            Long orderId = 1L;
            Long buyerId = 100L;
            Order order = createOrderWithMultipleProducts(orderId, buyerId, OrderStatus.PAID);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            PartialCancelOrderCommand command = createPartialCancelCommand(orderId, buyerId, List.of(999L));

            assertThatThrownBy(() -> partialCancelOrderService.partialCancelOrder(command))
                    .isInstanceOf(InvalidPartialCancelProductException.class);

            verifyNoInteractions(updateOrderPort);
            verifyNoInteractions(orderCancelSagaStarter);
        }

        @Test
        @DisplayName("이미 취소된 상품을 다시 부분 취소 요청하면 InvalidPartialCancelProductException이 발생한다")
        void partialCancelOrder_alreadyCancelledProduct_throwsException() {
            Long orderId = 1L;
            Long buyerId = 100L;
            Order order = createOrderWithPartiallyCancelledProducts(orderId, buyerId);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            // pricePolicyId 100L은 이미 CANCELLED 상태
            PartialCancelOrderCommand command = createPartialCancelCommand(orderId, buyerId, List.of(100L));

            assertThatThrownBy(() -> partialCancelOrderService.partialCancelOrder(command))
                    .isInstanceOf(InvalidPartialCancelProductException.class);

            verifyNoInteractions(updateOrderPort);
            verifyNoInteractions(orderCancelSagaStarter);
        }
    }

    // ==================================================================================
    // SAGA 미사용 — Sync 모드
    // ==================================================================================

    @Nested
    @DisplayName("SAGA 미사용 — Sync 모드")
    class SyncModeTest {

        private PartialCancelOrderService syncPartialCancelOrderService;

        @BeforeEach
        void setUp() {
            syncPartialCancelOrderService = new PartialCancelOrderService(
                    getOrderUseCase,
                    cancelOrderUseCase,
                    updateOrderPort,
                    cancelFulfillmentReleasePort,
                    publishOrderEventPort,
                    transactionManager,
                    clock,
                    Optional.empty()
            );
        }

        @Test
        @DisplayName("SAGA 미사용 시 풀필먼트 취소 후 상태 변경하고 이벤트를 발행한다")
        void partialCancelOrder_syncMode_cancelsAndPublishesEvent() {
            Long orderId = 1L;
            Long buyerId = 100L;
            Order order = createOrderWithMultipleProducts(orderId, buyerId, OrderStatus.PREPARING);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);
            when(cancelFulfillmentReleasePort.cancelRelease(orderId))
                    .thenReturn(new CancelFulfillmentReleaseResult(orderId, true, "취소 성공"));

            PartialCancelOrderCommand command = createPartialCancelCommand(orderId, buyerId, List.of(100L));

            syncPartialCancelOrderService.partialCancelOrder(command);

            verify(cancelFulfillmentReleasePort).cancelRelease(orderId);
            verify(updateOrderPort).update(any(Order.class), any(OrderStatusHistory.class));
            verify(publishOrderEventPort).publishOrderCancelledEvent(
                    eq(orderId), any(), eq(buyerId),
                    any(), any(), any(), any(),
                    eq(false), eq(0L),
                    any(), any()
            );
        }

        @Test
        @DisplayName("결제 대기 상태에서 Sync 부분 취소 시 풀필먼트 취소와 이벤트 발행 없이 상태만 변경한다")
        void partialCancelOrder_fromPaymentPending_syncMode() {
            Long orderId = 1L;
            Long buyerId = 100L;
            Order order = createOrderWithMultipleProducts(orderId, buyerId, OrderStatus.PAYMENT_PENDING);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            PartialCancelOrderCommand command = createPartialCancelCommand(orderId, buyerId, List.of(100L));

            syncPartialCancelOrderService.partialCancelOrder(command);

            verify(updateOrderPort).update(any(Order.class), any(OrderStatusHistory.class));
            verifyNoInteractions(cancelFulfillmentReleasePort);
            verifyNoInteractions(publishOrderEventPort);
        }
    }

    // ==================================================================================
    // 헬퍼 메서드
    // ==================================================================================

    private PartialCancelOrderCommand createPartialCancelCommand(Long orderId, Long buyerId, List<Long> cancelPricePolicyIds) {
        return PartialCancelOrderCommand.builder()
                .id(orderId)
                .reasonCategory(OrderStatusReasonCategory.CANCEL_ORDER)
                .reason("구매 의사 취소")
                .buyerId(buyerId)
                .cancelPricePolicyIds(cancelPricePolicyIds)
                .build();
    }

    /**
     * 주문 상품 2개(pricePolicyId: 100L, 200L)를 가진 주문 생성
     */
    private Order createOrderWithMultipleProducts(Long orderId, Long buyerId, OrderStatus status) {
        List<OrderProductSnapshotState> productStates = List.of(
                OrderProductSnapshotState.builder()
                        .orderId(orderId)
                        .sellerId(10L)
                        .pricePolicyId(100L)
                        .sharerKey(UUID.randomUUID())
                        .quantity(1)
                        .unitAmount(30000L)
                        .orderStatus(status)
                        .accumulatedPoint(0L)
                        .build(),
                OrderProductSnapshotState.builder()
                        .orderId(orderId)
                        .sellerId(20L)
                        .pricePolicyId(200L)
                        .sharerKey(UUID.randomUUID())
                        .quantity(2)
                        .unitAmount(20000L)
                        .orderStatus(status)
                        .accumulatedPoint(0L)
                        .build()
        );

        return Order.from(OrderSnapshotState.builder()
                .id(orderId)
                .buyerId(buyerId)
                .orderKey(UUID.randomUUID())
                .orderNumber(OrderNumber.of("ORD-" + orderId))
                .orderStatus(status)
                .amount(OrderAmount.of(70000L, 70000L, 0L, 0L, 3000L))
                .shippingAddress(ShippingAddress.of("수령인", "010-1234-5678", "12345", "서울시 강남구", "상세주소", null, null))
                .orderProductStates(productStates)
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build());
    }

    /**
     * 부분 취소된 주문 생성 (pricePolicyId 100L=CANCELLED, 200L=PAID, 300L=PAID)
     */
    private Order createOrderWithPartiallyCancelledProducts(Long orderId, Long buyerId) {
        List<OrderProductSnapshotState> productStates = List.of(
                OrderProductSnapshotState.builder()
                        .orderId(orderId)
                        .sellerId(10L)
                        .pricePolicyId(100L)
                        .sharerKey(UUID.randomUUID())
                        .quantity(1)
                        .unitAmount(30000L)
                        .orderStatus(OrderStatus.CANCELLED)
                        .accumulatedPoint(0L)
                        .build(),
                OrderProductSnapshotState.builder()
                        .orderId(orderId)
                        .sellerId(20L)
                        .pricePolicyId(200L)
                        .sharerKey(UUID.randomUUID())
                        .quantity(2)
                        .unitAmount(20000L)
                        .orderStatus(OrderStatus.PAID)
                        .accumulatedPoint(0L)
                        .build(),
                OrderProductSnapshotState.builder()
                        .orderId(orderId)
                        .sellerId(30L)
                        .pricePolicyId(300L)
                        .sharerKey(UUID.randomUUID())
                        .quantity(1)
                        .unitAmount(15000L)
                        .orderStatus(OrderStatus.PAID)
                        .accumulatedPoint(0L)
                        .build()
        );

        return Order.from(OrderSnapshotState.builder()
                .id(orderId)
                .buyerId(buyerId)
                .orderKey(UUID.randomUUID())
                .orderNumber(OrderNumber.of("ORD-" + orderId))
                .orderStatus(OrderStatus.PARTIALLY_CANCELLED)
                .amount(OrderAmount.of(65000L, 65000L, 0L, 0L, 3000L))
                .shippingAddress(ShippingAddress.of("수령인", "010-1234-5678", "12345", "서울시 강남구", "상세주소", null, null))
                .orderProductStates(productStates)
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build());
    }
}
