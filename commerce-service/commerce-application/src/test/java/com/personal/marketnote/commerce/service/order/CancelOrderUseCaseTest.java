package com.personal.marketnote.commerce.service.order;

import com.personal.marketnote.commerce.domain.order.*;
import com.personal.marketnote.commerce.exception.*;
import com.personal.marketnote.commerce.port.in.command.order.CancelOrderCommand;
import com.personal.marketnote.commerce.port.in.command.saga.OrderCancelSagaContext;
import com.personal.marketnote.commerce.port.in.usecase.order.GetOrderUseCase;
import com.personal.marketnote.commerce.port.out.order.UpdateOrderPort;
import com.personal.marketnote.commerce.service.saga.OrderCancelSagaStarter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
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
    @Mock
    private OrderCancelSagaStarter orderCancelSagaStarter;

    private CancelOrderService cancelOrderService;

    @BeforeEach
    void setUp() {
        cancelOrderService = new CancelOrderService(
                getOrderUseCase,
                updateOrderPort,
                orderCancelSagaStarter
        );
    }

    // ==================================================================================
    // 결제 대기 상태 취소 — 즉시 CANCELLED (SAGA 미호출)
    // ==================================================================================

    @Nested
    @DisplayName("결제 대기 상태 취소")
    class PaymentPendingCancelTest {

        @Test
        @DisplayName("결제 대기 상태의 주문을 취소하면 SAGA 호출 없이 즉시 CANCELLED로 전이되고 이력이 기록된다")
        void cancelOrder_fromPaymentPending_cancelledWithoutSaga() {
            Long orderId = 1L;
            Long buyerId = 100L;
            Order order = createOrder(orderId, buyerId, OrderStatus.PAYMENT_PENDING);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            CancelOrderCommand command = createCommand(orderId, buyerId);

            cancelOrderService.cancelOrder(command);

            ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
            ArgumentCaptor<OrderStatusHistory> historyCaptor = ArgumentCaptor.forClass(OrderStatusHistory.class);
            verify(updateOrderPort).update(orderCaptor.capture(), historyCaptor.capture());

            assertThat(orderCaptor.getValue().getOrderStatus()).isEqualTo(OrderStatus.CANCELLED);
            assertThat(historyCaptor.getValue().getOrderStatus()).isEqualTo(OrderStatus.CANCELLED);
            verifyNoInteractions(orderCancelSagaStarter);
        }
    }

    // ==================================================================================
    // SAGA 모드 — 결제 완료 상태 취소
    // ==================================================================================

    @Nested
    @DisplayName("SAGA 모드 — 결제 완료 상태 취소")
    class SagaModePaidCancelTest {

        @Test
        @DisplayName("결제 완료 상태의 주문을 취소하면 CANCEL_REQUESTED로 변경 후 SAGA Starter를 호출한다")
        void cancelOrder_fromPaid_startsSaga() {
            Long orderId = 1L;
            Long buyerId = 100L;
            Order order = createOrder(orderId, buyerId, OrderStatus.PAID);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            CancelOrderCommand command = createCommand(orderId, buyerId);

            cancelOrderService.cancelOrder(command);

            verify(updateOrderPort).update(any(Order.class), any(OrderStatusHistory.class));
            verify(orderCancelSagaStarter).start(any(OrderCancelSagaContext.class));
        }
    }

    // ==================================================================================
    // SAGA 모드 — 상품 준비중 상태 취소
    // ==================================================================================

    @Nested
    @DisplayName("SAGA 모드 — 상품 준비중 상태 취소")
    class SagaModePreparingCancelTest {

        @Test
        @DisplayName("상품 준비중 상태의 주문을 취소하면 CANCEL_REQUESTED로 변경 후 SAGA Starter를 호출한다")
        void cancelOrder_fromPreparing_startsSaga() {
            Long orderId = 1L;
            Long buyerId = 100L;
            Order order = createOrder(orderId, buyerId, OrderStatus.PREPARING);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            CancelOrderCommand command = createCommand(orderId, buyerId);

            cancelOrderService.cancelOrder(command);

            verify(updateOrderPort).update(any(Order.class), any(OrderStatusHistory.class));
            verify(orderCancelSagaStarter).start(any(OrderCancelSagaContext.class));
        }
    }

    // ==================================================================================
    // SAGA 컨텍스트 필드 검증
    // ==================================================================================

    @Nested
    @DisplayName("SAGA 컨텍스트 필드 검증")
    class SagaContextFieldVerificationTest {

        @Test
        @DisplayName("결제 완료 취소 시 OrderCancelSagaContext에 전체 취소 플래그와 원본 상태가 설정된다")
        void cancelOrder_fromPaid_contextHasCorrectFields() {
            Long orderId = 1L;
            Long buyerId = 100L;
            Order order = createOrder(orderId, buyerId, OrderStatus.PAID);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            CancelOrderCommand command = createCommand(orderId, buyerId);

            cancelOrderService.cancelOrder(command);

            ArgumentCaptor<OrderCancelSagaContext> captor = ArgumentCaptor.forClass(OrderCancelSagaContext.class);
            verify(orderCancelSagaStarter).start(captor.capture());
            OrderCancelSagaContext context = captor.getValue();

            assertThat(context.orderId()).isEqualTo(orderId);
            assertThat(context.orderKey()).isEqualTo(order.getOrderKey().toString());
            assertThat(context.buyerId()).isEqualTo(buyerId);
            assertThat(context.isFullCancel()).isTrue();
            assertThat(context.originalStatus()).isEqualTo(OrderStatus.PAID.name());
            assertThat(context.cancelAmount()).isEqualTo(order.getAmount().getPaidAmount().getValue());
            assertThat(context.paymentAmount()).isEqualTo(order.getAmount().getPaidAmount().getValue());
            assertThat(context.alreadyRefunded()).isZero();
            assertThat(context.reasonCategory()).isEqualTo(OrderStatusReasonCategory.CANCEL_ORDER.name());
            assertThat(context.orderProducts()).hasSize(1);
            assertThat(context.cancelProducts()).hasSize(1);
        }

        @Test
        @DisplayName("상품 준비중 취소 시 OrderCancelSagaContext의 원본 상태가 PREPARING으로 기록된다")
        void cancelOrder_fromPreparing_contextHasCorrectFields() {
            Long orderId = 1L;
            Long buyerId = 100L;
            Order order = createOrder(orderId, buyerId, OrderStatus.PREPARING);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            CancelOrderCommand command = createCommand(orderId, buyerId);

            cancelOrderService.cancelOrder(command);

            ArgumentCaptor<OrderCancelSagaContext> captor = ArgumentCaptor.forClass(OrderCancelSagaContext.class);
            verify(orderCancelSagaStarter).start(captor.capture());
            OrderCancelSagaContext context = captor.getValue();

            assertThat(context.originalStatus()).isEqualTo(OrderStatus.PREPARING.name());
            assertThat(context.isFullCancel()).isTrue();
            assertThat(context.orderId()).isEqualTo(orderId);
            assertThat(context.buyerId()).isEqualTo(buyerId);
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
            verifyNoInteractions(orderCancelSagaStarter);
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
            verifyNoInteractions(orderCancelSagaStarter);
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
            verifyNoInteractions(orderCancelSagaStarter);
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
            verifyNoInteractions(orderCancelSagaStarter);
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
            verifyNoInteractions(orderCancelSagaStarter);
        }

        @Test
        @DisplayName("PAYMENT_EXECUTING 상태에서 전체 취소 요청 시 InvalidOrderStatusTransitionException이 발생한다")
        void cancelOrder_fromPaymentExecuting_throwsInvalidTransitionException() {
            Long orderId = 1L;
            Long buyerId = 100L;
            Order order = createOrder(orderId, buyerId, OrderStatus.PAYMENT_EXECUTING);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            CancelOrderCommand command = createCommand(orderId, buyerId);

            assertThatThrownBy(() -> cancelOrderService.cancelOrder(command))
                    .isInstanceOf(InvalidOrderStatusTransitionException.class);

            verifyNoInteractions(updateOrderPort);
            verifyNoInteractions(orderCancelSagaStarter);
        }

        @Test
        @DisplayName("PAYMENT_EXECUTING 상태에서 부분 취소 요청 시 PartialCancelNotAllowedException이 발생한다")
        void cancelOrder_partialFromPaymentExecuting_throwsPartialCancelNotAllowedException() {
            Long orderId = 1L;
            Long buyerId = 100L;
            Order order = createOrder(orderId, buyerId, OrderStatus.PAYMENT_EXECUTING);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            CancelOrderCommand command = CancelOrderCommand.builder()
                    .id(orderId)
                    .buyerId(buyerId)
                    .reasonCategory(OrderStatusReasonCategory.CANCEL_ORDER)
                    .reason("구매 의사 취소")
                    .pricePolicyIds(List.of(100L))
                    .build();

            assertThatThrownBy(() -> cancelOrderService.cancelOrder(command))
                    .isInstanceOf(PartialCancelNotAllowedException.class);

            verifyNoInteractions(updateOrderPort);
            verifyNoInteractions(orderCancelSagaStarter);
        }
    }

    // ==================================================================================
    // 부분 취소 — pricePolicyIds 파라미터
    // ==================================================================================

    @Nested
    @DisplayName("부분 취소 — pricePolicyIds 파라미터")
    class PartialCancelTest {

        @Test
        @DisplayName("pricePolicyIds 일부 지정 시 PARTIALLY_CANCEL_REQUESTED로 전이 후 SAGA 호출한다")
        void cancelOrder_partialFromPaid_startsSagaWithPartialFlag() {
            Long orderId = 1L;
            Long buyerId = 100L;
            Order order = createOrderWithMultipleProducts(orderId, buyerId, OrderStatus.PAID);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            CancelOrderCommand command = CancelOrderCommand.builder()
                    .id(orderId)
                    .reasonCategory(OrderStatusReasonCategory.CANCEL_ORDER)
                    .reason("부분 취소")
                    .buyerId(buyerId)
                    .pricePolicyIds(List.of(100L))
                    .build();

            cancelOrderService.cancelOrder(command);

            ArgumentCaptor<OrderCancelSagaContext> captor = ArgumentCaptor.forClass(OrderCancelSagaContext.class);
            verify(orderCancelSagaStarter).start(captor.capture());
            OrderCancelSagaContext context = captor.getValue();

            assertThat(context.isFullCancel()).isFalse();
            assertThat(context.cancelProducts()).hasSize(1);
            assertThat(context.cancelProducts().get(0).pricePolicyId()).isEqualTo(100L);
            assertThat(context.productKeys()).isNotNull();
            verify(updateOrderPort).update(any(Order.class), any(OrderStatusHistory.class));
        }

        @Test
        @DisplayName("pricePolicyIds 전부 지정 시 전체 취소로 위임되어 isFullCancel=true로 SAGA 시작")
        void cancelOrder_allProductsSpecified_fullCancel() {
            Long orderId = 1L;
            Long buyerId = 100L;
            Order order = createOrderWithMultipleProducts(orderId, buyerId, OrderStatus.PAID);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            CancelOrderCommand command = CancelOrderCommand.builder()
                    .id(orderId)
                    .reasonCategory(OrderStatusReasonCategory.CANCEL_ORDER)
                    .reason("전체 취소")
                    .buyerId(buyerId)
                    .pricePolicyIds(List.of(100L, 200L))
                    .build();

            cancelOrderService.cancelOrder(command);

            ArgumentCaptor<OrderCancelSagaContext> captor = ArgumentCaptor.forClass(OrderCancelSagaContext.class);
            verify(orderCancelSagaStarter).start(captor.capture());
            assertThat(captor.getValue().isFullCancel()).isTrue();
        }

        @Test
        @DisplayName("pricePolicyIds 빈 리스트 = 전체 취소")
        void cancelOrder_emptyPricePolicyIds_fullCancel() {
            Long orderId = 1L;
            Long buyerId = 100L;
            Order order = createOrderWithMultipleProducts(orderId, buyerId, OrderStatus.PAID);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            CancelOrderCommand command = CancelOrderCommand.builder()
                    .id(orderId)
                    .reasonCategory(OrderStatusReasonCategory.CANCEL_ORDER)
                    .reason("전체 취소")
                    .buyerId(buyerId)
                    .pricePolicyIds(List.of())
                    .build();

            cancelOrderService.cancelOrder(command);

            ArgumentCaptor<OrderCancelSagaContext> captor = ArgumentCaptor.forClass(OrderCancelSagaContext.class);
            verify(orderCancelSagaStarter).start(captor.capture());
            assertThat(captor.getValue().isFullCancel()).isTrue();
        }

        @Test
        @DisplayName("pricePolicyIds에 주문에 없는 ID 포함 시 InvalidPartialCancelProductException 발생")
        void cancelOrder_nonExistentPricePolicyId_throwsException() {
            Long orderId = 1L;
            Long buyerId = 100L;
            Order order = createOrderWithMultipleProducts(orderId, buyerId, OrderStatus.PAID);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            CancelOrderCommand command = CancelOrderCommand.builder()
                    .id(orderId)
                    .reasonCategory(OrderStatusReasonCategory.CANCEL_ORDER)
                    .reason("부분 취소")
                    .buyerId(buyerId)
                    .pricePolicyIds(List.of(999L))
                    .build();

            assertThatThrownBy(() -> cancelOrderService.cancelOrder(command))
                    .isInstanceOf(InvalidPartialCancelProductException.class);

            verifyNoInteractions(updateOrderPort);
        }

        @Test
        @DisplayName("pricePolicyIds 중복 시 InvalidPartialCancelProductException 발생")
        void cancelOrder_duplicatePricePolicyIds_throwsException() {
            Long orderId = 1L;
            Long buyerId = 100L;
            Order order = createOrderWithMultipleProducts(orderId, buyerId, OrderStatus.PAID);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            CancelOrderCommand command = CancelOrderCommand.builder()
                    .id(orderId)
                    .reasonCategory(OrderStatusReasonCategory.CANCEL_ORDER)
                    .reason("부분 취소")
                    .buyerId(buyerId)
                    .pricePolicyIds(List.of(100L, 100L))
                    .build();

            assertThatThrownBy(() -> cancelOrderService.cancelOrder(command))
                    .isInstanceOf(InvalidPartialCancelProductException.class);

            verifyNoInteractions(updateOrderPort);
        }

        @Test
        @DisplayName("부분 취소 불가 상태(SHIPPING)에서 pricePolicyIds 요청 시 PartialCancelNotAllowedException 발생")
        void cancelOrder_partialFromShipping_throwsException() {
            Long orderId = 1L;
            Long buyerId = 100L;
            Order order = createOrderWithMultipleProducts(orderId, buyerId, OrderStatus.SHIPPING);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            CancelOrderCommand command = CancelOrderCommand.builder()
                    .id(orderId)
                    .reasonCategory(OrderStatusReasonCategory.CANCEL_ORDER)
                    .reason("부분 취소")
                    .buyerId(buyerId)
                    .pricePolicyIds(List.of(100L))
                    .build();

            assertThatThrownBy(() -> cancelOrderService.cancelOrder(command))
                    .isInstanceOf(PartialCancelNotAllowedException.class);

            verifyNoInteractions(updateOrderPort);
        }

        @Test
        @DisplayName("결제 대기 상태에서 일부 상품만 부분 취소하면 SAGA 없이 즉시 CANCELLED 처리된다")
        void cancelOrder_partialFromPaymentPending_persistsImmediatelyWithoutSaga() {
            Long orderId = 1L;
            Long buyerId = 100L;
            Order order = createOrderWithMultipleProducts(orderId, buyerId, OrderStatus.PAYMENT_PENDING);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            CancelOrderCommand command = CancelOrderCommand.builder()
                    .id(orderId)
                    .reasonCategory(OrderStatusReasonCategory.CANCEL_ORDER)
                    .reason("부분 취소")
                    .buyerId(buyerId)
                    .pricePolicyIds(List.of(100L))
                    .build();

            cancelOrderService.cancelOrder(command);

            ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
            verify(updateOrderPort).update(orderCaptor.capture(), any(OrderStatusHistory.class));

            Order updated = orderCaptor.getValue();
            OrderProduct cancelledProduct = updated.getOrderProducts().stream()
                    .filter(op -> op.getPricePolicyId().equals(100L))
                    .findFirst()
                    .orElseThrow();
            assertThat(cancelledProduct.isCancelled()).isTrue();
            verifyNoInteractions(orderCancelSagaStarter);
        }

        @Test
        @DisplayName("결제 대기 상태에서 모든 상품을 부분 취소 지정하면 전체 취소로 위임되어 SAGA 없이 즉시 CANCELLED 처리된다")
        void cancelOrder_partialAllFromPaymentPending_delegatesToFullCancelWithoutSaga() {
            Long orderId = 1L;
            Long buyerId = 100L;
            Order order = createOrderWithMultipleProducts(orderId, buyerId, OrderStatus.PAYMENT_PENDING);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            CancelOrderCommand command = CancelOrderCommand.builder()
                    .id(orderId)
                    .reasonCategory(OrderStatusReasonCategory.CANCEL_ORDER)
                    .reason("부분 취소")
                    .buyerId(buyerId)
                    .pricePolicyIds(List.of(100L, 200L))
                    .build();

            cancelOrderService.cancelOrder(command);

            ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
            verify(updateOrderPort).update(orderCaptor.capture(), any(OrderStatusHistory.class));

            assertThat(orderCaptor.getValue().getOrderStatus()).isEqualTo(OrderStatus.CANCELLED);
            verifyNoInteractions(orderCancelSagaStarter);
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
                        .accumulatedPoint(0L)
                        .build()
        );

        return Order.from(OrderSnapshotState.builder()
                .id(orderId)
                .buyerId(buyerId)
                .orderKey(UUID.randomUUID())
                .orderNumber(OrderNumber.of("ORD-" + orderId))
                .orderStatus(status)
                .amount(OrderAmount.of(50000L, 50000L, 0L, 0L, 3000L))
                .shippingAddress(ShippingAddress.of("수령인", "010-1234-5678", "12345", "서울시 강남구", "상세주소", null, null))
                .orderProductStates(productStates)
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build());
    }
}
