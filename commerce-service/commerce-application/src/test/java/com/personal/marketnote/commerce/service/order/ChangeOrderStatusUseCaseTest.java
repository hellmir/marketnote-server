package com.personal.marketnote.commerce.service.order;

import com.personal.marketnote.commerce.domain.order.*;
import com.personal.marketnote.commerce.exception.InvalidOrderStatusTransitionException;
import com.personal.marketnote.commerce.exception.OrderStatusAlreadyChangedException;
import com.personal.marketnote.commerce.port.in.command.order.ChangeOrderStatusCommand;
import com.personal.marketnote.commerce.port.in.usecase.order.GetOrderUseCase;
import com.personal.marketnote.commerce.port.out.event.PublishOrderEventPort;
import com.personal.marketnote.commerce.port.out.order.UpdateOrderPort;
import com.personal.marketnote.commerce.port.out.product.FindProductByPricePolicyPort;
import com.personal.marketnote.commerce.port.out.result.product.ProductInfoResult;
import com.personal.marketnote.commerce.port.out.reward.ModifyUserPointPort;
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
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChangeOrderStatusUseCaseTest {
    @Mock
    private GetOrderUseCase getOrderUseCase;
    @Mock
    private UpdateOrderPort updateOrderPort;
    @Mock
    private FindProductByPricePolicyPort findProductByPricePolicyPort;
    @Mock
    private ModifyUserPointPort modifyUserPointPort;
    @Mock
    private PublishOrderEventPort publishOrderEventPort;
    @Spy
    private Clock clock = Clock.fixed(Instant.parse("2026-03-01T00:00:00Z"), ZoneId.of("Asia/Seoul"));

    @InjectMocks
    private ChangeOrderStatusService changeOrderStatusService;

    @Nested
    @DisplayName("기본 상태 변경 성공")
    class BasicStatusChangeTest {

        @Test
        @DisplayName("PAYMENT_PENDING에서 PAID로 전체 변경하면 주문과 히스토리가 저장된다")
        void shouldSaveOrderAndHistoryWhenChangingFromPendingToPaid() {
            // given
            Long orderId = 1L;
            Order order = createOrder(orderId, 100L, OrderStatus.PAYMENT_PENDING, 10L);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            ChangeOrderStatusCommand command = ChangeOrderStatusCommand.builder()
                    .id(orderId)
                    .orderStatus(OrderStatus.PAID)
                    .build();

            // when
            changeOrderStatusService.changeOrderStatus(command);

            // then
            verify(updateOrderPort).update(eq(order), any(OrderStatusHistory.class));
            assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.PAID);
        }

        @Test
        @DisplayName("PAID에서 PREPARING으로 전체 변경하면 주문과 히스토리가 저장된다")
        void shouldSaveOrderAndHistoryWhenChangingFromPaidToPreparing() {
            // given
            Long orderId = 1L;
            Order order = createOrder(orderId, 100L, OrderStatus.PAID, 10L);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            ChangeOrderStatusCommand command = ChangeOrderStatusCommand.builder()
                    .id(orderId)
                    .orderStatus(OrderStatus.PREPARING)
                    .build();

            // when
            changeOrderStatusService.changeOrderStatus(command);

            // then
            verify(updateOrderPort).update(eq(order), any(OrderStatusHistory.class));
            assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.PREPARING);
        }

        @Test
        @DisplayName("DELIVERED에서 CONFIRMED로 전체 변경하면 주문과 히스토리가 저장된다")
        void shouldSaveOrderAndHistoryWhenChangingFromDeliveredToConfirmed() {
            // given
            Long orderId = 1L;
            Order order = createOrder(orderId, 100L, OrderStatus.DELIVERED, 10L);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            ChangeOrderStatusCommand command = ChangeOrderStatusCommand.builder()
                    .id(orderId)
                    .orderStatus(OrderStatus.CONFIRMED)
                    .build();

            // when
            changeOrderStatusService.changeOrderStatus(command);

            // then
            verify(updateOrderPort).update(eq(order), any(OrderStatusHistory.class));
            assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.CONFIRMED);
        }
    }

    @Nested
    @DisplayName("부분 상품 변경 (Partial Product Change)")
    class PartialProductChangeTest {

        @Test
        @DisplayName("pricePolicyIds가 있으면 해당 상품만 상태가 변경된다")
        void shouldChangeOnlySpecifiedProductsWhenPricePolicyIdsProvided() {
            // given
            Long orderId = 1L;
            Long pricePolicyId1 = 10L;
            Long pricePolicyId2 = 20L;
            Order order = createOrderWithMultipleProducts(orderId, 100L, OrderStatus.DELIVERED,
                    pricePolicyId1, pricePolicyId2);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            ChangeOrderStatusCommand command = ChangeOrderStatusCommand.builder()
                    .id(orderId)
                    .pricePolicyIds(List.of(pricePolicyId1))
                    .orderStatus(OrderStatus.CONFIRMED)
                    .build();

            // when
            changeOrderStatusService.changeOrderStatus(command);

            // then
            OrderProduct changedProduct = order.getOrderProducts().stream()
                    .filter(op -> op.getPricePolicyId().equals(pricePolicyId1))
                    .findFirst().orElseThrow();
            OrderProduct unchangedProduct = order.getOrderProducts().stream()
                    .filter(op -> op.getPricePolicyId().equals(pricePolicyId2))
                    .findFirst().orElseThrow();

            assertThat(changedProduct.getOrderStatus()).isEqualTo(OrderStatus.CONFIRMED);
            assertThat(unchangedProduct.getOrderStatus()).isEqualTo(OrderStatus.DELIVERED);
        }

        @Test
        @DisplayName("부분 변경 시 상태 전이 규칙 검증을 건너뛴다")
        void shouldSkipTransitionValidationForPartialChange() {
            // given
            Long orderId = 1L;
            Order order = createOrder(orderId, 100L, OrderStatus.DELIVERED, 10L);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            // DELIVERED → RETURNED는 전체 변경 시 canTransitionTo에서 불가능
            // 하지만 partial 변경에서는 전이 규칙을 건너뛰므로 주문 레벨 검증 없음
            // (개별 상품의 changeOrderStatus에서 검증)
            ChangeOrderStatusCommand command = ChangeOrderStatusCommand.builder()
                    .id(orderId)
                    .pricePolicyIds(List.of(10L))
                    .orderStatus(OrderStatus.CONFIRMED)
                    .build();

            // when & then
            assertThatCode(() -> changeOrderStatusService.changeOrderStatus(command))
                    .doesNotThrowAnyException();
            verify(updateOrderPort).update(eq(order), any(OrderStatusHistory.class));
        }

        @Test
        @DisplayName("부분 CONFIRMED 변경 시 updateConfirmSubsequentProcesses가 호출되지 않는다")
        void shouldNotCallConfirmSubsequentProcessesForPartialConfirmed() {
            // given
            Long orderId = 1L;
            Order order = createOrderWithMultipleProducts(orderId, 100L, OrderStatus.DELIVERED, 10L, 20L);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            ChangeOrderStatusCommand command = ChangeOrderStatusCommand.builder()
                    .id(orderId)
                    .pricePolicyIds(List.of(10L))
                    .orderStatus(OrderStatus.CONFIRMED)
                    .build();

            // when
            changeOrderStatusService.changeOrderStatus(command);

            // then
            verify(publishOrderEventPort, never()).publishOrderPurchaseConfirmedEvent(
                    anyLong(), anyLong(), anyList(), anyBoolean());
        }

        @Test
        @DisplayName("부분 RETURNED 변경 시 주문 상태가 PARTIALLY_RETURNED로 변경된다")
        void shouldChangeOrderStatusToPartiallyReturnedForPartialReturn() {
            // given
            Long orderId = 1L;
            Long pricePolicyId1 = 10L;
            Long pricePolicyId2 = 20L;
            Order order = createOrderWithMultipleProducts(orderId, 100L, OrderStatus.RETURN_IN_PROGRESS,
                    pricePolicyId1, pricePolicyId2);
            // 개별 상품도 RETURN_IN_PROGRESS로 설정
            order.changeAllProductsStatus(OrderStatus.RETURN_IN_PROGRESS, LocalDateTime.now(clock));
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            ChangeOrderStatusCommand command = ChangeOrderStatusCommand.builder()
                    .id(orderId)
                    .pricePolicyIds(List.of(pricePolicyId1))
                    .orderStatus(OrderStatus.RETURNED)
                    .build();

            // when
            changeOrderStatusService.changeOrderStatus(command);

            // then
            assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.PARTIALLY_RETURNED);
        }

        @Test
        @DisplayName("부분 CONFIRMED 변경 시 주문 상태가 PARTIALLY_CONFIRMED로 변경된다")
        void shouldChangeOrderStatusToPartiallyConfirmedForPartialConfirm() {
            // given
            Long orderId = 1L;
            Long pricePolicyId1 = 10L;
            Long pricePolicyId2 = 20L;
            Order order = createOrderWithMultipleProducts(orderId, 100L, OrderStatus.DELIVERED,
                    pricePolicyId1, pricePolicyId2);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            ChangeOrderStatusCommand command = ChangeOrderStatusCommand.builder()
                    .id(orderId)
                    .pricePolicyIds(List.of(pricePolicyId1))
                    .orderStatus(OrderStatus.CONFIRMED)
                    .build();

            // when
            changeOrderStatusService.changeOrderStatus(command);

            // then
            assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.PARTIALLY_CONFIRMED);
        }
    }

    @Nested
    @DisplayName("PAID 후속처리 스킵 조건")
    class PaidSubsequentProcessSkipTest {

        @Test
        @DisplayName("skipSubsequentProcesses가 true이면 PAID 후속처리가 실행되지 않는다")
        void shouldSkipPaidSubsequentProcessesWhenSkipIsTrue() {
            // given
            Long orderId = 1L;
            Order order = createOrder(orderId, 100L, OrderStatus.PAYMENT_PENDING, 10L);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            ChangeOrderStatusCommand command = ChangeOrderStatusCommand.builder()
                    .id(orderId)
                    .orderStatus(OrderStatus.PAID)
                    .skipSubsequentProcesses(true)
                    .build();

            // when
            changeOrderStatusService.changeOrderStatus(command);

            // then
            verify(findProductByPricePolicyPort, never()).findByPricePolicyIds(anyList());
            verify(modifyUserPointPort, never()).addPendingProductAccumulationPoints(anyLong(), anyLong(), anyLong());
            verify(publishOrderEventPort, never()).publishOrderPaymentCompletedEvent(
                    anyLong(), anyLong(), anyLong(), anyLong(), anyList(), anyLong());
        }

        @Test
        @DisplayName("skipSubsequentProcesses가 null이면 PAID 후속처리가 정상 실행된다")
        void shouldExecutePaidSubsequentProcessesWhenSkipIsNull() {
            // given
            Long orderId = 1L;
            Order order = createOrder(orderId, 100L, OrderStatus.PAYMENT_PENDING, 10L, 500L);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            ChangeOrderStatusCommand command = ChangeOrderStatusCommand.builder()
                    .id(orderId)
                    .orderStatus(OrderStatus.PAID)
                    .skipSubsequentProcesses(null)
                    .build();

            // when
            changeOrderStatusService.changeOrderStatus(command);

            // then
            verify(publishOrderEventPort).publishOrderPaymentCompletedEvent(
                    eq(orderId), eq(100L), eq(50000L), eq(0L), eq(order.getOrderProducts()), eq(500L));
            verify(modifyUserPointPort).addPendingProductAccumulationPoints(100L, 500L, orderId);
        }
    }

    @Nested
    @DisplayName("CONFIRMED 후속처리 (이벤트 발행)")
    class ConfirmedSubsequentProcessTest {

        @Test
        @DisplayName("CONFIRMED로 전체 변경하면 publishOrderPurchaseConfirmedEvent가 호출된다")
        void shouldPublishConfirmedEventWhenChangingToConfirmed() {
            // given
            Long orderId = 1L;
            Long buyerId = 100L;
            Order order = createOrder(orderId, buyerId, OrderStatus.DELIVERED, 10L);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            ChangeOrderStatusCommand command = ChangeOrderStatusCommand.builder()
                    .id(orderId)
                    .orderStatus(OrderStatus.CONFIRMED)
                    .build();

            // when
            changeOrderStatusService.changeOrderStatus(command);

            // then
            verify(publishOrderEventPort).publishOrderPurchaseConfirmedEvent(
                    eq(orderId), eq(buyerId), eq(List.of()), eq(true));
        }

        @Test
        @DisplayName("주문 상품에 sharerKey가 있으면 이벤트에 sharerKeys가 포함된다")
        void shouldIncludeSharerKeysInEventWhenOrderProductsHaveSharerKey() {
            // given
            Long orderId = 1L;
            Long buyerId = 100L;
            UUID sharerKey = UUID.randomUUID();
            Order order = createOrderWithSharer(orderId, buyerId, OrderStatus.DELIVERED, 10L, sharerKey);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            ChangeOrderStatusCommand command = ChangeOrderStatusCommand.builder()
                    .id(orderId)
                    .orderStatus(OrderStatus.CONFIRMED)
                    .build();

            // when
            changeOrderStatusService.changeOrderStatus(command);

            // then
            verify(publishOrderEventPort).publishOrderPurchaseConfirmedEvent(
                    eq(orderId), eq(buyerId), eq(List.of(sharerKey)), eq(true));
        }

        @Test
        @DisplayName("주문 상품에 sharerKey가 없으면 빈 리스트로 이벤트가 발행된다")
        void shouldPublishEventWithEmptySharerKeysWhenNoSharerKey() {
            // given
            Long orderId = 1L;
            Long buyerId = 100L;
            Order order = createOrder(orderId, buyerId, OrderStatus.DELIVERED, 10L);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            ChangeOrderStatusCommand command = ChangeOrderStatusCommand.builder()
                    .id(orderId)
                    .orderStatus(OrderStatus.CONFIRMED)
                    .build();

            // when
            changeOrderStatusService.changeOrderStatus(command);

            // then
            verify(publishOrderEventPort).publishOrderPurchaseConfirmedEvent(
                    eq(orderId), eq(buyerId), eq(List.of()), eq(true));
        }

        @Test
        @DisplayName("publishOrderPurchaseConfirmedEvent 호출 실패 시 예외가 전파되지 않는다")
        void shouldNotPropagateExceptionWhenPublishConfirmedEventFails() {
            // given
            Long orderId = 1L;
            Order order = createOrder(orderId, 100L, OrderStatus.DELIVERED, 10L);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);
            doThrow(new RuntimeException("이벤트 발행 실패"))
                    .when(publishOrderEventPort).publishOrderPurchaseConfirmedEvent(
                            anyLong(), anyLong(), anyList(), anyBoolean());

            ChangeOrderStatusCommand command = ChangeOrderStatusCommand.builder()
                    .id(orderId)
                    .orderStatus(OrderStatus.CONFIRMED)
                    .build();

            // when & then
            assertThatCode(() -> changeOrderStatusService.changeOrderStatus(command))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("PAID 후속처리 - Outbox 이벤트 발행")
    class PaidOutboxEventTest {

        @Test
        @DisplayName("PAID로 변경하면 publishOrderPaymentCompletedEvent가 호출된다")
        void shouldPublishPaymentCompletedEventWhenChangingToPaid() {
            // given
            Long orderId = 1L;
            Long buyerId = 100L;
            Order order = createOrder(orderId, buyerId, OrderStatus.PAYMENT_PENDING, 10L, 500L);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            ChangeOrderStatusCommand command = ChangeOrderStatusCommand.builder()
                    .id(orderId)
                    .orderStatus(OrderStatus.PAID)
                    .build();

            // when
            changeOrderStatusService.changeOrderStatus(command);

            // then
            verify(publishOrderEventPort).publishOrderPaymentCompletedEvent(
                    eq(orderId), eq(buyerId), eq(50000L), eq(0L), eq(order.getOrderProducts()), eq(500L));
        }

        @Test
        @DisplayName("publishOrderPaymentCompletedEvent 호출 실패 시 예외가 전파되지 않는다")
        void shouldNotPropagateExceptionWhenPublishPaymentCompletedEventFails() {
            // given
            Long orderId = 1L;
            Order order = createOrder(orderId, 100L, OrderStatus.PAYMENT_PENDING, 10L, 500L);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);
            doThrow(new RuntimeException("Outbox 저장 실패"))
                    .when(publishOrderEventPort).publishOrderPaymentCompletedEvent(
                            anyLong(), anyLong(), anyLong(), anyLong(), anyList(), anyLong());

            ChangeOrderStatusCommand command = ChangeOrderStatusCommand.builder()
                    .id(orderId)
                    .orderStatus(OrderStatus.PAID)
                    .build();

            // when & then
            assertThatCode(() -> changeOrderStatusService.changeOrderStatus(command))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("스냅샷 적립 포인트 우선 사용")
    class SnapshotAccumulatedPointTest {

        @Test
        @DisplayName("모든 OrderProduct에 accumulatedPoint가 있으면 상품 서비스를 조회하지 않는다")
        void shouldNotCallProductServiceWhenAllProductsHaveSnapshotPoint() {
            // given
            Long orderId = 1L;
            Long buyerId = 100L;
            Order order = createOrderWithSnapshotPoints(orderId, buyerId, 10L, 500L, 20L, 300L);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            ChangeOrderStatusCommand command = ChangeOrderStatusCommand.builder()
                    .id(orderId)
                    .orderStatus(OrderStatus.PAID)
                    .build();

            // when
            changeOrderStatusService.changeOrderStatus(command);

            // then
            verify(findProductByPricePolicyPort, never()).findByPricePolicyIds(anyList());
            // 500 * 1 + 300 * 1 = 800
            verify(modifyUserPointPort).addPendingProductAccumulationPoints(buyerId, 800L, orderId);
        }

        @Test
        @DisplayName("일부 OrderProduct의 accumulatedPoint가 0이면 0으로 합산에 포함된다")
        void shouldIncludeZeroAccumulatedPointInSum() {
            // given
            Long orderId = 1L;
            Long buyerId = 100L;
            Long pricePolicyId1 = 10L;
            Long pricePolicyId2 = 20L;
            Order order = createOrderWithSnapshotPoints(orderId, buyerId, pricePolicyId1, 500L, pricePolicyId2, 0L);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            ChangeOrderStatusCommand command = ChangeOrderStatusCommand.builder()
                    .id(orderId)
                    .orderStatus(OrderStatus.PAID)
                    .build();

            // when
            changeOrderStatusService.changeOrderStatus(command);

            // then
            verify(findProductByPricePolicyPort, never()).findByPricePolicyIds(anyList());
            verify(modifyUserPointPort).addPendingProductAccumulationPoints(buyerId, 500L, orderId);
        }
    }

    @Nested
    @DisplayName("PARTIALLY_CONFIRMED/PARTIALLY_RETURNED 멱등성")
    class PartialStatusIdempotencyTest {

        @Test
        @DisplayName("PARTIALLY_CONFIRMED 상태에서 CONFIRMED로 변경하면 OrderStatusAlreadyChangedException이 발생하지 않는다")
        void shouldNotThrowWhenChangingFromPartiallyConfirmedToConfirmed() {
            // given
            Long orderId = 1L;
            Order order = createOrder(orderId, 100L, OrderStatus.PARTIALLY_CONFIRMED, 10L);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            ChangeOrderStatusCommand command = ChangeOrderStatusCommand.builder()
                    .id(orderId)
                    .orderStatus(OrderStatus.CONFIRMED)
                    .build();

            // when & then
            assertThatCode(() -> changeOrderStatusService.changeOrderStatus(command))
                    .doesNotThrowAnyException();
            assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.CONFIRMED);
        }

        @Test
        @DisplayName("PARTIALLY_RETURNED 상태에서 RETURNED로 변경하면 OrderStatusAlreadyChangedException이 발생하지 않는다")
        void shouldNotThrowWhenChangingFromPartiallyReturnedToReturned() {
            // given
            Long orderId = 1L;
            Order order = createOrderWithProductStatus(orderId, 100L, OrderStatus.PARTIALLY_RETURNED,
                    10L, OrderStatus.RETURN_IN_PROGRESS);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            ChangeOrderStatusCommand command = ChangeOrderStatusCommand.builder()
                    .id(orderId)
                    .orderStatus(OrderStatus.RETURNED)
                    .build();

            // when & then
            assertThatCode(() -> changeOrderStatusService.changeOrderStatus(command))
                    .doesNotThrowAnyException();
            assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.RETURNED);
        }
    }

    @Nested
    @DisplayName("상태 변경 실패")
    class StatusChangeFailureTest {

        @Test
        @DisplayName("동일 상태로 변경하면 OrderStatusAlreadyChangedException이 발생한다")
        void shouldThrowWhenChangingToSameStatus() {
            // given
            Long orderId = 1L;
            Order order = createOrder(orderId, 100L, OrderStatus.PAID, 10L);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            ChangeOrderStatusCommand command = ChangeOrderStatusCommand.builder()
                    .id(orderId)
                    .orderStatus(OrderStatus.PAID)
                    .build();

            // when & then
            assertThatThrownBy(() -> changeOrderStatusService.changeOrderStatus(command))
                    .isInstanceOf(OrderStatusAlreadyChangedException.class);
        }

        @Test
        @DisplayName("허용되지 않은 상태 전이 시 InvalidOrderStatusTransitionException이 발생한다")
        void shouldThrowWhenTransitionIsNotAllowed() {
            // given
            Long orderId = 1L;
            Order order = createOrder(orderId, 100L, OrderStatus.PAID, 10L);
            when(getOrderUseCase.getOrder(orderId)).thenReturn(order);

            ChangeOrderStatusCommand command = ChangeOrderStatusCommand.builder()
                    .id(orderId)
                    .orderStatus(OrderStatus.DELIVERED)
                    .build();

            // when & then
            assertThatThrownBy(() -> changeOrderStatusService.changeOrderStatus(command))
                    .isInstanceOf(InvalidOrderStatusTransitionException.class);
        }
    }

    // ==================================================================================
    // 헬퍼 메서드
    // ==================================================================================

    private Order createOrder(Long orderId, Long buyerId, OrderStatus status, Long pricePolicyId) {
        return createOrder(orderId, buyerId, status, pricePolicyId, 0L);
    }

    private Order createOrder(Long orderId, Long buyerId, OrderStatus status, Long pricePolicyId, Long accumulatedPoint) {
        List<OrderProductSnapshotState> productStates = List.of(
                OrderProductSnapshotState.builder()
                        .orderId(orderId)
                        .sellerId(10L)
                        .pricePolicyId(pricePolicyId)
                        .quantity(1)
                        .unitAmount(50000L)
                        .orderStatus(status)
                        .accumulatedPoint(accumulatedPoint)
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

    private Order createOrderWithProductStatus(Long orderId, Long buyerId, OrderStatus orderStatus,
                                               Long pricePolicyId, OrderStatus productStatus) {
        List<OrderProductSnapshotState> productStates = List.of(
                OrderProductSnapshotState.builder()
                        .orderId(orderId)
                        .sellerId(10L)
                        .pricePolicyId(pricePolicyId)
                        .quantity(1)
                        .unitAmount(50000L)
                        .orderStatus(productStatus)
                        .accumulatedPoint(0L)
                        .build()
        );

        return Order.from(OrderSnapshotState.builder()
                .id(orderId)
                .buyerId(buyerId)
                .orderKey(UUID.randomUUID())
                .orderNumber("ORD-" + orderId)
                .orderStatus(orderStatus)
                .amount(OrderAmount.of(50000L, null, 0L, 0L, null))
                .shippingAddress(ShippingAddress.of("수령인", "01012345678", "12345", "서울시 강남구", "상세주소", null, null))
                .orderProductStates(productStates)
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build());
    }

    private Order createOrderWithSharer(Long orderId, Long buyerId, OrderStatus status,
                                        Long pricePolicyId, UUID sharerKey) {
        List<OrderProductSnapshotState> productStates = List.of(
                OrderProductSnapshotState.builder()
                        .orderId(orderId)
                        .sellerId(10L)
                        .pricePolicyId(pricePolicyId)
                        .sharerKey(sharerKey)
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
                .orderNumber("ORD-" + orderId)
                .orderStatus(status)
                .amount(OrderAmount.of(50000L, null, 0L, 0L, null))
                .shippingAddress(ShippingAddress.of("수령인", "01012345678", "12345", "서울시 강남구", "상세주소", null, null))
                .orderProductStates(productStates)
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build());
    }

    private Order createOrderWithMultipleProducts(Long orderId, Long buyerId, OrderStatus status,
                                                  Long pricePolicyId1, Long pricePolicyId2) {
        List<OrderProductSnapshotState> productStates = List.of(
                OrderProductSnapshotState.builder()
                        .orderId(orderId)
                        .sellerId(10L)
                        .pricePolicyId(pricePolicyId1)
                        .quantity(1)
                        .unitAmount(50000L)
                        .orderStatus(status)
                        .accumulatedPoint(0L)
                        .build(),
                OrderProductSnapshotState.builder()
                        .orderId(orderId)
                        .sellerId(10L)
                        .pricePolicyId(pricePolicyId2)
                        .quantity(1)
                        .unitAmount(30000L)
                        .orderStatus(status)
                        .accumulatedPoint(0L)
                        .build()
        );

        return Order.from(OrderSnapshotState.builder()
                .id(orderId)
                .buyerId(buyerId)
                .orderKey(UUID.randomUUID())
                .orderNumber("ORD-" + orderId)
                .orderStatus(status)
                .amount(OrderAmount.of(80000L, null, 0L, 0L, null))
                .shippingAddress(ShippingAddress.of("수령인", "01012345678", "12345", "서울시 강남구", "상세주소", null, null))
                .orderProductStates(productStates)
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build());
    }

    private Order createOrderWithSnapshotPoints(Long orderId, Long buyerId,
                                                Long pricePolicyId1, Long point1,
                                                Long pricePolicyId2, Long point2) {
        List<OrderProductSnapshotState> productStates = List.of(
                OrderProductSnapshotState.builder()
                        .orderId(orderId)
                        .sellerId(10L)
                        .pricePolicyId(pricePolicyId1)
                        .quantity(1)
                        .unitAmount(50000L)
                        .accumulatedPoint(point1)
                        .orderStatus(OrderStatus.PAYMENT_PENDING)
                        .build(),
                OrderProductSnapshotState.builder()
                        .orderId(orderId)
                        .sellerId(10L)
                        .pricePolicyId(pricePolicyId2)
                        .quantity(1)
                        .unitAmount(30000L)
                        .accumulatedPoint(point2)
                        .orderStatus(OrderStatus.PAYMENT_PENDING)
                        .build()
        );

        return Order.from(OrderSnapshotState.builder()
                .id(orderId)
                .buyerId(buyerId)
                .orderKey(UUID.randomUUID())
                .orderNumber("ORD-" + orderId)
                .orderStatus(OrderStatus.PAYMENT_PENDING)
                .amount(OrderAmount.of(80000L, null, 0L, 0L, null))
                .shippingAddress(ShippingAddress.of("수령인", "01012345678", "12345", "서울시 강남구", "상세주소", null, null))
                .orderProductStates(productStates)
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build());
    }

    private ProductInfoResult createProductInfoResult(Long accumulatedPoint) {
        return new ProductInfoResult(1L, 1L, "테스트 상품", "브랜드", 50000L, null, accumulatedPoint, List.of());
    }
}
