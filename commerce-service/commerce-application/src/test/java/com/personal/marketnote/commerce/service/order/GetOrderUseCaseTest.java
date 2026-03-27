package com.personal.marketnote.commerce.service.order;

import com.personal.marketnote.commerce.domain.order.*;
import com.personal.marketnote.commerce.exception.OrderNotFoundException;
import com.personal.marketnote.commerce.port.out.order.FindOrderPort;
import com.personal.marketnote.commerce.port.out.order.FindOrderProductPort;
import com.personal.marketnote.commerce.port.out.product.FindProductByPricePolicyPort;
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
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetOrderUseCaseTest {
    @Mock
    private FindOrderPort findOrderPort;
    @Mock
    private FindOrderProductPort findOrderProductPort;
    @Mock
    private FindProductByPricePolicyPort findProductByPricePolicyPort;

    @Spy
    private Clock clock = Clock.fixed(Instant.parse("2026-03-01T00:00:00Z"), ZoneId.of("Asia/Seoul"));

    @InjectMocks
    private GetOrderService getOrderService;

    // ==================================================================================
    // 주문 조회 성공 케이스
    // ==================================================================================

    @Nested
    @DisplayName("주문 조회 성공 케이스")
    class GetOrderSuccessTest {

        @Test
        @DisplayName("주문 ID로 주문을 조회하면 Order 도메인을 반환한다")
        void getOrder_exists_returnsOrder() {
            Long orderId = 1L;
            Order order = createOrder(orderId);

            when(findOrderPort.findById(orderId)).thenReturn(Optional.of(order));

            Order result = getOrderService.getOrder(orderId);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(orderId);
        }

        @Test
        @DisplayName("주문 조회 시 FindOrderPort.findById를 호출한다")
        void getOrder_callsFindOrderPort() {
            Long orderId = 1L;
            Order order = createOrder(orderId);

            when(findOrderPort.findById(orderId)).thenReturn(Optional.of(order));

            getOrderService.getOrder(orderId);

            verify(findOrderPort).findById(orderId);
        }

        @Test
        @DisplayName("주문 조회 시 FindOrderPort.findById를 정확히 한 번 호출한다")
        void getOrder_callsFindOrderPortExactlyOnce() {
            Long orderId = 1L;
            Order order = createOrder(orderId);

            when(findOrderPort.findById(orderId)).thenReturn(Optional.of(order));

            getOrderService.getOrder(orderId);

            verify(findOrderPort, times(1)).findById(orderId);
            verifyNoMoreInteractions(findOrderPort);
        }

        @Test
        @DisplayName("주문 조회 시 다른 Port와 상호작용하지 않는다")
        void getOrder_doesNotInteractWithOtherPorts() {
            Long orderId = 1L;
            Order order = createOrder(orderId);

            when(findOrderPort.findById(orderId)).thenReturn(Optional.of(order));

            getOrderService.getOrder(orderId);

            verifyNoInteractions(findOrderProductPort);
            verifyNoInteractions(findProductByPricePolicyPort);
        }
    }

    // ==================================================================================
    // Order 속성 검증
    // ==================================================================================

    @Nested
    @DisplayName("Order 속성 검증")
    class OrderPropertyTest {

        @Test
        @DisplayName("반환된 Order의 id가 조회한 주문과 일치한다")
        void getOrder_returnsOrderWithCorrectId() {
            Long orderId = 999L;
            Order order = createOrder(orderId);

            when(findOrderPort.findById(orderId)).thenReturn(Optional.of(order));

            Order result = getOrderService.getOrder(orderId);

            assertThat(result.getId()).isEqualTo(orderId);
        }

        @Test
        @DisplayName("반환된 Order의 buyerId가 조회한 주문과 일치한다")
        void getOrder_returnsOrderWithCorrectBuyerId() {
            Long orderId = 1L;
            Long buyerId = 123L;
            Order order = createOrderWithBuyerId(orderId, buyerId);

            when(findOrderPort.findById(orderId)).thenReturn(Optional.of(order));

            Order result = getOrderService.getOrder(orderId);

            assertThat(result.getBuyerId()).isEqualTo(buyerId);
        }

        @Test
        @DisplayName("반환된 Order의 orderKey가 조회한 주문과 일치한다")
        void getOrder_returnsOrderWithCorrectOrderKey() {
            Long orderId = 1L;
            UUID orderKey = UUID.randomUUID();
            Order order = createOrderWithOrderKey(orderId, orderKey);

            when(findOrderPort.findById(orderId)).thenReturn(Optional.of(order));

            Order result = getOrderService.getOrder(orderId);

            assertThat(result.getOrderKey()).isEqualTo(orderKey);
        }

        @Test
        @DisplayName("반환된 Order의 orderNumber가 조회한 주문과 일치한다")
        void getOrder_returnsOrderWithCorrectOrderNumber() {
            Long orderId = 1L;
            String orderNumber = "ORD-2025-12345";
            Order order = createOrderWithOrderNumber(orderId, orderNumber);

            when(findOrderPort.findById(orderId)).thenReturn(Optional.of(order));

            Order result = getOrderService.getOrder(orderId);

            assertThat(result.getOrderNumber()).isEqualTo(orderNumber);
        }

        @Test
        @DisplayName("반환된 Order의 orderStatus가 조회한 주문과 일치한다")
        void getOrder_returnsOrderWithCorrectOrderStatus() {
            Long orderId = 1L;
            OrderStatus orderStatus = OrderStatus.PAID;
            Order order = createOrderWithStatus(orderId, orderStatus);

            when(findOrderPort.findById(orderId)).thenReturn(Optional.of(order));

            Order result = getOrderService.getOrder(orderId);

            assertThat(result.getOrderStatus()).isEqualTo(orderStatus);
        }

        @Test
        @DisplayName("반환된 Order의 statusChangeReasonCategory가 조회한 주문과 일치한다")
        void getOrder_returnsOrderWithCorrectStatusChangeReasonCategory() {
            Long orderId = 1L;
            OrderStatusReasonCategory reasonCategory = OrderStatusReasonCategory.CANCEL_ORDER;
            Order order = createOrderWithStatusChangeReason(orderId, reasonCategory, "취소 사유");

            when(findOrderPort.findById(orderId)).thenReturn(Optional.of(order));

            Order result = getOrderService.getOrder(orderId);

            assertThat(result.getStatusChangeReasonCategory()).isEqualTo(reasonCategory);
        }

        @Test
        @DisplayName("반환된 Order의 statusChangeReason이 조회한 주문과 일치한다")
        void getOrder_returnsOrderWithCorrectStatusChangeReason() {
            Long orderId = 1L;
            String reason = "단순 변심으로 인한 취소";
            Order order = createOrderWithStatusChangeReason(orderId, OrderStatusReasonCategory.CANCEL_ORDER, reason);

            when(findOrderPort.findById(orderId)).thenReturn(Optional.of(order));

            Order result = getOrderService.getOrder(orderId);

            assertThat(result.getStatusChangeReason()).isEqualTo(reason);
        }

        @Test
        @DisplayName("반환된 Order의 totalAmount가 조회한 주문과 일치한다")
        void getOrder_returnsOrderWithCorrectTotalAmount() {
            Long orderId = 1L;
            Long totalAmount = 150000L;
            Order order = createOrderWithAmounts(orderId, totalAmount, null, 0L, 0L);

            when(findOrderPort.findById(orderId)).thenReturn(Optional.of(order));

            Order result = getOrderService.getOrder(orderId);

            assertThat(result.getAmount().getTotalAmount()).isEqualTo(totalAmount);
        }

        @Test
        @DisplayName("반환된 Order의 paidAmount가 조회한 주문과 일치한다")
        void getOrder_returnsOrderWithCorrectPaidAmount() {
            Long orderId = 1L;
            Long paidAmount = 135000L;
            Order order = createOrderWithAmounts(orderId, 150000L, paidAmount, 10000L, 5000L);

            when(findOrderPort.findById(orderId)).thenReturn(Optional.of(order));

            Order result = getOrderService.getOrder(orderId);

            assertThat(result.getAmount().getPaidAmount()).isEqualTo(paidAmount);
        }

        @Test
        @DisplayName("반환된 Order의 couponAmount가 조회한 주문과 일치한다")
        void getOrder_returnsOrderWithCorrectCouponAmount() {
            Long orderId = 1L;
            Long couponAmount = 10000L;
            Order order = createOrderWithAmounts(orderId, 150000L, null, couponAmount, 0L);

            when(findOrderPort.findById(orderId)).thenReturn(Optional.of(order));

            Order result = getOrderService.getOrder(orderId);

            assertThat(result.getAmount().getCouponAmount()).isEqualTo(couponAmount);
        }

        @Test
        @DisplayName("반환된 Order의 pointAmount가 조회한 주문과 일치한다")
        void getOrder_returnsOrderWithCorrectPointAmount() {
            Long orderId = 1L;
            Long pointAmount = 5000L;
            Order order = createOrderWithAmounts(orderId, 150000L, null, 0L, pointAmount);

            when(findOrderPort.findById(orderId)).thenReturn(Optional.of(order));

            Order result = getOrderService.getOrder(orderId);

            assertThat(result.getAmount().getPointAmount()).isEqualTo(pointAmount);
        }

        @Test
        @DisplayName("반환된 Order의 orderProducts가 조회한 주문과 일치한다")
        void getOrder_returnsOrderWithCorrectOrderProducts() {
            Long orderId = 1L;
            Order order = createOrderWithProducts(orderId, List.of(100L, 200L, 300L));

            when(findOrderPort.findById(orderId)).thenReturn(Optional.of(order));

            Order result = getOrderService.getOrder(orderId);

            assertThat(result.getOrderProducts()).hasSize(3);
            assertThat(result.getOrderProducts())
                    .extracting(OrderProduct::getPricePolicyId)
                    .containsExactly(100L, 200L, 300L);
        }

        @Test
        @DisplayName("반환된 Order의 createdAt이 조회한 주문과 일치한다")
        void getOrder_returnsOrderWithCorrectCreatedAt() {
            Long orderId = 1L;
            LocalDateTime createdAt = LocalDateTime.of(2025, 1, 15, 10, 30, 0);
            Order order = createOrderWithTimestamps(orderId, createdAt, null);

            when(findOrderPort.findById(orderId)).thenReturn(Optional.of(order));

            Order result = getOrderService.getOrder(orderId);

            assertThat(result.getCreatedAt()).isEqualTo(createdAt);
        }

        @Test
        @DisplayName("반환된 Order의 modifiedAt이 조회한 주문과 일치한다")
        void getOrder_returnsOrderWithCorrectModifiedAt() {
            Long orderId = 1L;
            LocalDateTime modifiedAt = LocalDateTime.of(2025, 1, 20, 14, 45, 0);
            Order order = createOrderWithTimestamps(orderId, LocalDateTime.now(), modifiedAt);

            when(findOrderPort.findById(orderId)).thenReturn(Optional.of(order));

            Order result = getOrderService.getOrder(orderId);

            assertThat(result.getModifiedAt()).isEqualTo(modifiedAt);
        }
    }

    // ==================================================================================
    // 주문 상태별 조회 검증
    // ==================================================================================

    @Nested
    @DisplayName("주문 상태별 조회 검증")
    class OrderStatusTest {

        @Test
        @DisplayName("PAYMENT_PENDING 상태의 주문을 정상 조회한다")
        void getOrder_paymentPending_succeeds() {
            Long orderId = 1L;
            Order order = createOrderWithStatus(orderId, OrderStatus.PAYMENT_PENDING);

            when(findOrderPort.findById(orderId)).thenReturn(Optional.of(order));

            Order result = getOrderService.getOrder(orderId);

            assertThat(result.getOrderStatus()).isEqualTo(OrderStatus.PAYMENT_PENDING);
        }

        @Test
        @DisplayName("PAID 상태의 주문을 정상 조회한다")
        void getOrder_paid_succeeds() {
            Long orderId = 1L;
            Order order = createOrderWithStatus(orderId, OrderStatus.PAID);

            when(findOrderPort.findById(orderId)).thenReturn(Optional.of(order));

            Order result = getOrderService.getOrder(orderId);

            assertThat(result.getOrderStatus()).isEqualTo(OrderStatus.PAID);
        }

        @Test
        @DisplayName("PREPARING 상태의 주문을 정상 조회한다")
        void getOrder_preparing_succeeds() {
            Long orderId = 1L;
            Order order = createOrderWithStatus(orderId, OrderStatus.PREPARING);

            when(findOrderPort.findById(orderId)).thenReturn(Optional.of(order));

            Order result = getOrderService.getOrder(orderId);

            assertThat(result.getOrderStatus()).isEqualTo(OrderStatus.PREPARING);
        }

        @Test
        @DisplayName("SHIPPING 상태의 주문을 정상 조회한다")
        void getOrder_shipping_succeeds() {
            Long orderId = 1L;
            Order order = createOrderWithStatus(orderId, OrderStatus.SHIPPING);

            when(findOrderPort.findById(orderId)).thenReturn(Optional.of(order));

            Order result = getOrderService.getOrder(orderId);

            assertThat(result.getOrderStatus()).isEqualTo(OrderStatus.SHIPPING);
        }

        @Test
        @DisplayName("DELIVERED 상태의 주문을 정상 조회한다")
        void getOrder_delivered_succeeds() {
            Long orderId = 1L;
            Order order = createOrderWithStatus(orderId, OrderStatus.DELIVERED);

            when(findOrderPort.findById(orderId)).thenReturn(Optional.of(order));

            Order result = getOrderService.getOrder(orderId);

            assertThat(result.getOrderStatus()).isEqualTo(OrderStatus.DELIVERED);
        }

        @Test
        @DisplayName("CONFIRMED 상태의 주문을 정상 조회한다")
        void getOrder_confirmed_succeeds() {
            Long orderId = 1L;
            Order order = createOrderWithStatus(orderId, OrderStatus.CONFIRMED);

            when(findOrderPort.findById(orderId)).thenReturn(Optional.of(order));

            Order result = getOrderService.getOrder(orderId);

            assertThat(result.getOrderStatus()).isEqualTo(OrderStatus.CONFIRMED);
        }

        @Test
        @DisplayName("CANCELLED 상태의 주문을 정상 조회한다")
        void getOrder_cancelled_succeeds() {
            Long orderId = 1L;
            Order order = createOrderWithStatus(orderId, OrderStatus.CANCELLED);

            when(findOrderPort.findById(orderId)).thenReturn(Optional.of(order));

            Order result = getOrderService.getOrder(orderId);

            assertThat(result.getOrderStatus()).isEqualTo(OrderStatus.CANCELLED);
        }

        @Test
        @DisplayName("REFUNDED 상태의 주문을 정상 조회한다")
        void getOrder_refunded_succeeds() {
            Long orderId = 1L;
            Order order = createOrderWithStatus(orderId, OrderStatus.REFUNDED);

            when(findOrderPort.findById(orderId)).thenReturn(Optional.of(order));

            Order result = getOrderService.getOrder(orderId);

            assertThat(result.getOrderStatus()).isEqualTo(OrderStatus.REFUNDED);
        }

        @Test
        @DisplayName("PARTIALLY_CONFIRMED 상태의 주문을 정상 조회한다")
        void getOrder_partiallyConfirmed_succeeds() {
            Long orderId = 1L;
            Order order = createOrderWithStatus(orderId, OrderStatus.PARTIALLY_CONFIRMED);

            when(findOrderPort.findById(orderId)).thenReturn(Optional.of(order));

            Order result = getOrderService.getOrder(orderId);

            assertThat(result.getOrderStatus()).isEqualTo(OrderStatus.PARTIALLY_CONFIRMED);
        }

        @Test
        @DisplayName("PARTIALLY_REFUNDED 상태의 주문을 정상 조회한다")
        void getOrder_partiallyRefunded_succeeds() {
            Long orderId = 1L;
            Order order = createOrderWithStatus(orderId, OrderStatus.PARTIALLY_REFUNDED);

            when(findOrderPort.findById(orderId)).thenReturn(Optional.of(order));

            Order result = getOrderService.getOrder(orderId);

            assertThat(result.getOrderStatus()).isEqualTo(OrderStatus.PARTIALLY_REFUNDED);
        }
    }

    // ==================================================================================
    // 주문 조회 실패 케이스
    // ==================================================================================

    @Nested
    @DisplayName("주문 조회 실패 케이스")
    class GetOrderFailureTest {

        @Test
        @DisplayName("주문이 존재하지 않으면 OrderNotFoundException이 발생한다")
        void getOrder_notFound_throwsOrderNotFoundException() {
            Long orderId = 999L;

            when(findOrderPort.findById(orderId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> getOrderService.getOrder(orderId))
                    .isInstanceOf(OrderNotFoundException.class);
        }

        @Test
        @DisplayName("OrderNotFoundException 메시지에 주문 ID가 포함된다")
        void getOrder_notFound_exceptionContainsOrderId() {
            Long orderId = 12345L;

            when(findOrderPort.findById(orderId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> getOrderService.getOrder(orderId))
                    .isInstanceOf(OrderNotFoundException.class)
                    .hasMessageContaining("12345");
        }

        @Test
        @DisplayName("OrderNotFoundException 메시지에 안내 문구가 포함된다")
        void getOrder_notFound_exceptionContainsGuideMessage() {
            Long orderId = 1L;

            when(findOrderPort.findById(orderId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> getOrderService.getOrder(orderId))
                    .isInstanceOf(OrderNotFoundException.class)
                    .hasMessageContaining("주문을 찾을 수 없습니다");
        }
    }

    // ==================================================================================
    // 예외 전파 케이스
    // ==================================================================================

    @Nested
    @DisplayName("예외 전파 케이스")
    class ExceptionPropagationTest {

        @Test
        @DisplayName("FindOrderPort.findById에서 RuntimeException 발생 시 예외를 전파한다")
        void getOrder_findOrderPortThrowsRuntimeException_propagates() {
            Long orderId = 1L;
            RuntimeException exception = new RuntimeException("DB 연결 실패");

            when(findOrderPort.findById(orderId)).thenThrow(exception);

            assertThatThrownBy(() -> getOrderService.getOrder(orderId))
                    .isSameAs(exception);
        }

        @Test
        @DisplayName("FindOrderPort.findById에서 IllegalStateException 발생 시 예외를 전파한다")
        void getOrder_findOrderPortThrowsIllegalStateException_propagates() {
            Long orderId = 1L;
            IllegalStateException exception = new IllegalStateException("잘못된 상태");

            when(findOrderPort.findById(orderId)).thenThrow(exception);

            assertThatThrownBy(() -> getOrderService.getOrder(orderId))
                    .isSameAs(exception);
        }

        @Test
        @DisplayName("FindOrderPort.findById에서 NullPointerException 발생 시 예외를 전파한다")
        void getOrder_findOrderPortThrowsNullPointerException_propagates() {
            Long orderId = 1L;
            NullPointerException exception = new NullPointerException("null 참조");

            when(findOrderPort.findById(orderId)).thenThrow(exception);

            assertThatThrownBy(() -> getOrderService.getOrder(orderId))
                    .isSameAs(exception);
        }
    }

    // ==================================================================================
    // 호출 검증
    // ==================================================================================

    @Nested
    @DisplayName("호출 검증")
    class InvocationTest {

        @Test
        @DisplayName("전달받은 주문 ID로 FindOrderPort.findById를 호출한다")
        void getOrder_passesCorrectIdToFindOrderPort() {
            Long orderId = 777L;
            Order order = createOrder(orderId);

            when(findOrderPort.findById(orderId)).thenReturn(Optional.of(order));

            getOrderService.getOrder(orderId);

            verify(findOrderPort).findById(777L);
        }

        @Test
        @DisplayName("서로 다른 주문 ID로 조회 시 각각 올바른 ID로 호출한다")
        void getOrder_differentIds_callsWithCorrectIds() {
            Order order1 = createOrder(1L);
            Order order2 = createOrder(2L);
            Order order3 = createOrder(3L);

            when(findOrderPort.findById(1L)).thenReturn(Optional.of(order1));
            when(findOrderPort.findById(2L)).thenReturn(Optional.of(order2));
            when(findOrderPort.findById(3L)).thenReturn(Optional.of(order3));

            getOrderService.getOrder(1L);
            getOrderService.getOrder(2L);
            getOrderService.getOrder(3L);

            verify(findOrderPort).findById(1L);
            verify(findOrderPort).findById(2L);
            verify(findOrderPort).findById(3L);
        }

        @Test
        @DisplayName("동일한 주문 ID로 여러 번 조회 시 매번 FindOrderPort를 호출한다")
        void getOrder_sameIdMultipleTimes_callsPortEachTime() {
            Long orderId = 1L;
            Order order = createOrder(orderId);

            when(findOrderPort.findById(orderId)).thenReturn(Optional.of(order));

            getOrderService.getOrder(orderId);
            getOrderService.getOrder(orderId);
            getOrderService.getOrder(orderId);

            verify(findOrderPort, times(3)).findById(orderId);
        }
    }

    // ==================================================================================
    // 엣지 케이스
    // ==================================================================================

    @Nested
    @DisplayName("엣지 케이스")
    class EdgeCaseTest {

        @Test
        @DisplayName("주문 ID가 1인 경우 정상 조회된다")
        void getOrder_minId_succeeds() {
            Long orderId = 1L;
            Order order = createOrder(orderId);

            when(findOrderPort.findById(orderId)).thenReturn(Optional.of(order));

            Order result = getOrderService.getOrder(orderId);

            assertThat(result.getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("주문 ID가 큰 값인 경우 정상 조회된다")
        void getOrder_largeId_succeeds() {
            Long orderId = Long.MAX_VALUE - 1;
            Order order = createOrder(orderId);

            when(findOrderPort.findById(orderId)).thenReturn(Optional.of(order));

            Order result = getOrderService.getOrder(orderId);

            assertThat(result.getId()).isEqualTo(orderId);
        }

        @Test
        @DisplayName("주문 상품이 빈 목록인 경우 빈 목록을 가진 Order를 반환한다")
        void getOrder_emptyOrderProducts_returnsOrderWithEmptyProducts() {
            Long orderId = 1L;
            Order order = createOrderWithProducts(orderId, List.of());

            when(findOrderPort.findById(orderId)).thenReturn(Optional.of(order));

            Order result = getOrderService.getOrder(orderId);

            assertThat(result.getOrderProducts()).isEmpty();
        }

        @Test
        @DisplayName("totalAmount가 0인 주문도 정상 조회된다")
        void getOrder_zeroTotalAmount_succeeds() {
            Long orderId = 1L;
            Order order = createOrderWithAmounts(orderId, 0L, null, 0L, 0L);

            when(findOrderPort.findById(orderId)).thenReturn(Optional.of(order));

            Order result = getOrderService.getOrder(orderId);

            assertThat(result.getAmount().getTotalAmount()).isEqualTo(0L);
        }

        @Test
        @DisplayName("totalAmount가 null인 주문도 정상 조회된다")
        void getOrder_nullTotalAmount_succeeds() {
            Long orderId = 1L;
            Order order = createOrderWithAmounts(orderId, null, null, null, null);

            when(findOrderPort.findById(orderId)).thenReturn(Optional.of(order));

            Order result = getOrderService.getOrder(orderId);

            assertThat(result.getAmount().getTotalAmount()).isNull();
        }

        @Test
        @DisplayName("매우 큰 totalAmount를 가진 주문도 정상 조회된다")
        void getOrder_largeTotalAmount_succeeds() {
            Long orderId = 1L;
            Long largeTotalAmount = Long.MAX_VALUE - 1;
            Order order = createOrderWithAmounts(orderId, largeTotalAmount, null, 0L, 0L);

            when(findOrderPort.findById(orderId)).thenReturn(Optional.of(order));

            Order result = getOrderService.getOrder(orderId);

            assertThat(result.getAmount().getTotalAmount()).isEqualTo(largeTotalAmount);
        }

        @Test
        @DisplayName("statusChangeReason이 null인 주문도 정상 조회된다")
        void getOrder_nullStatusChangeReason_succeeds() {
            Long orderId = 1L;
            Order order = createOrderWithStatusChangeReason(orderId, null, null);

            when(findOrderPort.findById(orderId)).thenReturn(Optional.of(order));

            Order result = getOrderService.getOrder(orderId);

            assertThat(result.getStatusChangeReasonCategory()).isNull();
            assertThat(result.getStatusChangeReason()).isNull();
        }

        @Test
        @DisplayName("많은 주문 상품을 가진 주문도 정상 조회된다")
        void getOrder_manyOrderProducts_succeeds() {
            Long orderId = 1L;
            List<Long> pricePolicyIds = new java.util.ArrayList<>();
            for (long i = 1; i <= 100; i++) {
                pricePolicyIds.add(i);
            }
            Order order = createOrderWithProducts(orderId, pricePolicyIds);

            when(findOrderPort.findById(orderId)).thenReturn(Optional.of(order));

            Order result = getOrderService.getOrder(orderId);

            assertThat(result.getOrderProducts()).hasSize(100);
        }

        @Test
        @DisplayName("조회 결과 Order 객체는 FindOrderPort가 반환한 것과 동일하다")
        void getOrder_returnsSameObjectFromPort() {
            Long orderId = 1L;
            Order order = createOrder(orderId);

            when(findOrderPort.findById(orderId)).thenReturn(Optional.of(order));

            Order result = getOrderService.getOrder(orderId);

            assertThat(result).isSameAs(order);
        }
    }

    // ==================================================================================
    // 헬퍼 메서드
    // ==================================================================================

    private Order createOrder(Long orderId) {
        return Order.from(OrderSnapshotState.builder()
                .id(orderId)
                .buyerId(1L)
                .orderKey(UUID.randomUUID())
                .orderNumber("ORD-" + orderId)
                .orderStatus(OrderStatus.PAID)
                .amount(OrderAmount.of(100000L, null, 0L, 0L, null))
                .shippingAddress(ShippingAddress.of("수령인", "01012345678", "12345", "서울시 강남구", "상세주소", null, null))
                .orderProductStates(List.of())
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build());
    }

    private Order createOrderWithBuyerId(Long orderId, Long buyerId) {
        return Order.from(OrderSnapshotState.builder()
                .id(orderId)
                .buyerId(buyerId)
                .orderKey(UUID.randomUUID())
                .orderNumber("ORD-" + orderId)
                .orderStatus(OrderStatus.PAID)
                .amount(OrderAmount.of(100000L, null, 0L, 0L, null))
                .shippingAddress(ShippingAddress.of("수령인", "01012345678", "12345", "서울시 강남구", "상세주소", null, null))
                .orderProductStates(List.of())
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build());
    }

    private Order createOrderWithOrderKey(Long orderId, UUID orderKey) {
        return Order.from(OrderSnapshotState.builder()
                .id(orderId)
                .buyerId(1L)
                .orderKey(orderKey)
                .orderNumber("ORD-" + orderId)
                .orderStatus(OrderStatus.PAID)
                .amount(OrderAmount.of(100000L, null, 0L, 0L, null))
                .shippingAddress(ShippingAddress.of("수령인", "01012345678", "12345", "서울시 강남구", "상세주소", null, null))
                .orderProductStates(List.of())
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build());
    }

    private Order createOrderWithOrderNumber(Long orderId, String orderNumber) {
        return Order.from(OrderSnapshotState.builder()
                .id(orderId)
                .buyerId(1L)
                .orderKey(UUID.randomUUID())
                .orderNumber(orderNumber)
                .orderStatus(OrderStatus.PAID)
                .amount(OrderAmount.of(100000L, null, 0L, 0L, null))
                .shippingAddress(ShippingAddress.of("수령인", "01012345678", "12345", "서울시 강남구", "상세주소", null, null))
                .orderProductStates(List.of())
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build());
    }

    private Order createOrderWithStatus(Long orderId, OrderStatus status) {
        return Order.from(OrderSnapshotState.builder()
                .id(orderId)
                .buyerId(1L)
                .orderKey(UUID.randomUUID())
                .orderNumber("ORD-" + orderId)
                .orderStatus(status)
                .amount(OrderAmount.of(100000L, null, 0L, 0L, null))
                .shippingAddress(ShippingAddress.of("수령인", "01012345678", "12345", "서울시 강남구", "상세주소", null, null))
                .orderProductStates(List.of())
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build());
    }

    private Order createOrderWithStatusChangeReason(Long orderId, OrderStatusReasonCategory reasonCategory, String reason) {
        return Order.from(OrderSnapshotState.builder()
                .id(orderId)
                .buyerId(1L)
                .orderKey(UUID.randomUUID())
                .orderNumber("ORD-" + orderId)
                .orderStatus(OrderStatus.CANCELLED)
                .statusChangeReasonCategory(reasonCategory)
                .statusChangeReason(reason)
                .amount(OrderAmount.of(100000L, null, 0L, 0L, null))
                .shippingAddress(ShippingAddress.of("수령인", "01012345678", "12345", "서울시 강남구", "상세주소", null, null))
                .orderProductStates(List.of())
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build());
    }

    private Order createOrderWithAmounts(Long orderId, Long totalAmount, Long paidAmount, Long couponAmount, Long pointAmount) {
        return Order.from(OrderSnapshotState.builder()
                .id(orderId)
                .buyerId(1L)
                .orderKey(UUID.randomUUID())
                .orderNumber("ORD-" + orderId)
                .orderStatus(OrderStatus.PAID)
                .amount(OrderAmount.of(totalAmount, paidAmount, couponAmount, pointAmount, null))
                .shippingAddress(ShippingAddress.of("수령인", "01012345678", "12345", "서울시 강남구", "상세주소", null, null))
                .orderProductStates(List.of())
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build());
    }

    private Order createOrderWithProducts(Long orderId, List<Long> pricePolicyIds) {
        List<OrderProductSnapshotState> productStates = pricePolicyIds.stream()
                .map(pricePolicyId -> OrderProductSnapshotState.builder()
                        .orderId(orderId)
                        .sellerId(10L)
                        .pricePolicyId(pricePolicyId)
                        .quantity(1)
                        .unitAmount(50000L)
                        .orderStatus(OrderStatus.PAID)
                        .build())
                .toList();

        return Order.from(OrderSnapshotState.builder()
                .id(orderId)
                .buyerId(1L)
                .orderKey(UUID.randomUUID())
                .orderNumber("ORD-" + orderId)
                .orderStatus(OrderStatus.PAID)
                .amount(OrderAmount.of(100000L, null, 0L, 0L, null))
                .shippingAddress(ShippingAddress.of("수령인", "01012345678", "12345", "서울시 강남구", "상세주소", null, null))
                .orderProductStates(productStates)
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build());
    }

    private Order createOrderWithTimestamps(Long orderId, LocalDateTime createdAt, LocalDateTime modifiedAt) {
        return Order.from(OrderSnapshotState.builder()
                .id(orderId)
                .buyerId(1L)
                .orderKey(UUID.randomUUID())
                .orderNumber("ORD-" + orderId)
                .orderStatus(OrderStatus.PAID)
                .amount(OrderAmount.of(100000L, null, 0L, 0L, null))
                .shippingAddress(ShippingAddress.of("수령인", "01012345678", "12345", "서울시 강남구", "상세주소", null, null))
                .orderProductStates(List.of())
                .createdAt(createdAt)
                .modifiedAt(modifiedAt)
                .build());
    }
}
