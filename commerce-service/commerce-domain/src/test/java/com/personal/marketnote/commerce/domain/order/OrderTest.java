package com.personal.marketnote.commerce.domain.order;

import com.personal.marketnote.common.domain.delivery.DeliveryRequestType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OrderTest {

    @Test
    @DisplayName("CreateState로 생성하면 초기 상태가 PAYMENT_PENDING이다")
    void shouldSetInitialStatusToPaymentPendingWhenCreatedFromCreateState() {
        OrderCreateState state = createDefaultOrderCreateState();

        Order order = Order.from(state);

        assertThat(order.isPaymentPending()).isTrue();
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.PAYMENT_PENDING);
    }

    @Test
    @DisplayName("CreateState로 생성하면 orderKey와 orderNumber가 생성된다")
    void shouldGenerateOrderKeyAndOrderNumber() {
        OrderCreateState state = createDefaultOrderCreateState();

        Order order = Order.from(state);

        assertThat(order.getOrderKey()).isNotNull();
        assertThat(order.getOrderNumber()).isNotNull();
        assertThat(order.getOrderNumber()).isNotBlank();
    }

    @Test
    @DisplayName("CreateState에 orderProductStates가 null이면 빈 리스트로 설정된다")
    void shouldSetEmptyProductsWhenCreateStateProductsIsNull() {
        OrderCreateState state = OrderCreateState.builder()
                .buyerId(1L)
                .amount(createOrderAmount())
                .shippingAddress(createShippingAddress())
                .orderProductStates(null)
                .build();

        Order order = Order.from(state);

        assertThat(order.getOrderProducts()).isEmpty();
    }

    @Test
    @DisplayName("SnapshotState로 복원하면 모든 필드가 올바르게 매핑된다")
    void shouldRestoreAllFieldsFromSnapshotState() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime modifiedAt = LocalDateTime.of(2026, 4, 1, 12, 0);
        UUID orderKey = UUID.randomUUID();
        OrderSnapshotState state = OrderSnapshotState.builder()
                .id(1L)
                .buyerId(100L)
                .orderKey(orderKey)
                .orderNumber("ORD-20260101-001")
                .orderStatus(OrderStatus.PAID)
                .statusChangeReasonCategory(null)
                .statusChangeReason(null)
                .amount(createOrderAmount())
                .shippingAddress(createShippingAddress())
                .pickupAddress(null)
                .orderProductStates(List.of())
                .createdAt(createdAt)
                .modifiedAt(modifiedAt)
                .build();

        Order order = Order.from(state);

        assertThat(order.getId()).isEqualTo(1L);
        assertThat(order.getBuyerId()).isEqualTo(100L);
        assertThat(order.getOrderKey()).isEqualTo(orderKey);
        assertThat(order.getOrderNumber()).isEqualTo("ORD-20260101-001");
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.PAID);
        assertThat(order.getCreatedAt()).isEqualTo(createdAt);
        assertThat(order.getModifiedAt()).isEqualTo(modifiedAt);
    }

    @Test
    @DisplayName("isPaymentPending은 PAYMENT_PENDING 상태에서 true를 반환한다")
    void shouldReturnTrueWhenStatusIsPaymentPending() {
        Order order = createOrderWithStatus(OrderStatus.PAYMENT_PENDING);

        assertThat(order.isPaymentPending()).isTrue();
        assertThat(order.isFailed()).isFalse();
    }

    @Test
    @DisplayName("isFailed는 FAILED 상태에서 true를 반환한다")
    void shouldReturnTrueWhenStatusIsFailed() {
        Order order = createOrderWithStatus(OrderStatus.FAILED);

        assertThat(order.isFailed()).isTrue();
        assertThat(order.isPaymentPending()).isFalse();
    }

    @Test
    @DisplayName("isBuyer는 동일 buyerId이면 true를 반환한다")
    void shouldReturnTrueWhenBuyerIdMatches() {
        Order order = createOrderWithStatus(OrderStatus.PAID);

        assertThat(order.isBuyer(100L)).isTrue();
    }

    @Test
    @DisplayName("isBuyer는 다른 buyerId이면 false를 반환한다")
    void shouldReturnFalseWhenBuyerIdDoesNotMatch() {
        Order order = createOrderWithStatus(OrderStatus.PAID);

        assertThat(order.isBuyer(999L)).isFalse();
    }

    @Test
    @DisplayName("applyPickupAddress에 수령인이 있는 주소를 전달하면 해당 주소가 설정된다")
    void shouldApplyPickupAddressWhenRecipientNameExists() {
        Order order = createOrderWithStatus(OrderStatus.DELIVERED);
        ShippingAddress pickupAddress = ShippingAddress.of(
                "회수 담당자", "010-9999-8888", "12345", "회수 주소", "201호",
                null, null
        );

        order.applyPickupAddress(pickupAddress);

        assertThat(order.getPickupAddress()).isEqualTo(pickupAddress);
        assertThat(order.getPickupAddress().getRecipientName()).isEqualTo("회수 담당자");
    }

    @Test
    @DisplayName("applyPickupAddress에 수령인이 없는 주소를 전달하면 배송지에서 배송 요청 제거한 주소가 설정된다")
    void shouldFallbackToShippingAddressWithoutDeliveryRequestWhenNoRecipientName() {
        Order order = createOrderWithStatus(OrderStatus.DELIVERED);
        ShippingAddress pickupAddress = ShippingAddress.of(
                null, null, null, null, null, null, null
        );

        order.applyPickupAddress(pickupAddress);

        assertThat(order.getPickupAddress().getRecipientName()).isEqualTo("홍길동");
        assertThat(order.getPickupAddress().getDeliveryRequestType()).isNull();
        assertThat(order.getPickupAddress().getDeliveryRequestMessage()).isNull();
    }

    @Test
    @DisplayName("applyPickupAddress에 null을 전달하면 배송지에서 배송 요청 제거한 주소가 설정된다")
    void shouldFallbackToShippingAddressWhenPickupAddressIsNull() {
        Order order = createOrderWithStatus(OrderStatus.DELIVERED);

        order.applyPickupAddress(null);

        assertThat(order.getPickupAddress().getRecipientName()).isEqualTo("홍길동");
        assertThat(order.getPickupAddress().getDeliveryRequestType()).isNull();
    }

    @Test
    @DisplayName("changeAllProductsStatus를 호출하면 모든 상품과 주문 상태가 변경된다")
    void shouldChangeAllProductsAndOrderStatus() {
        Order order = createOrderWithProducts(OrderStatus.PAYMENT_PENDING);
        LocalDateTime now = LocalDateTime.of(2026, 4, 11, 12, 0);

        order.changeAllProductsStatus(OrderStatus.PAID, now);

        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.PAID);
        order.getOrderProducts().forEach(product ->
                assertThat(product.getOrderStatus()).isEqualTo(OrderStatus.PAID)
        );
    }

    @Test
    @DisplayName("changeProductsStatus에서 일부만 반품하면 PARTIALLY_RETURNED가 된다")
    void shouldSetPartiallyReturnedWhenOnlySomeProductsReturned() {
        Order order = createOrderWithReturnInProgressProducts();
        LocalDateTime now = LocalDateTime.of(2026, 4, 11, 12, 0);

        order.changeProductsStatus(List.of(1L), OrderStatus.RETURNED, now);

        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.PARTIALLY_RETURNED);
        assertThat(order.getOrderProducts().get(0).getOrderStatus()).isEqualTo(OrderStatus.RETURNED);
        assertThat(order.getOrderProducts().get(1).getOrderStatus()).isEqualTo(OrderStatus.RETURN_IN_PROGRESS);
    }

    @Test
    @DisplayName("changeProductsStatus에서 일부만 확정하면 PARTIALLY_CONFIRMED이 된다")
    void shouldSetPartiallyConfirmedWhenOnlySomeProductsConfirmed() {
        Order order = createOrderWithDeliveredProducts();
        LocalDateTime now = LocalDateTime.of(2026, 4, 11, 12, 0);

        order.changeProductsStatus(List.of(1L), OrderStatus.CONFIRMED, now);

        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.PARTIALLY_CONFIRMED);
        assertThat(order.getOrderProducts().get(0).getOrderStatus()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(order.getOrderProducts().get(1).getOrderStatus()).isEqualTo(OrderStatus.DELIVERED);
    }

    @Test
    @DisplayName("changeProductsStatus에서 전체 동일 상태면 해당 상태로 변경된다")
    void shouldSetOrderStatusWhenAllProductsHaveSameStatus() {
        Order order = createOrderWithDeliveredProducts();
        LocalDateTime now = LocalDateTime.of(2026, 4, 11, 12, 0);

        order.changeProductsStatus(List.of(1L, 2L), OrderStatus.CONFIRMED, now);

        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.CONFIRMED);
        order.getOrderProducts().forEach(product ->
                assertThat(product.getOrderStatus()).isEqualTo(OrderStatus.CONFIRMED)
        );
    }

    private OrderCreateState createDefaultOrderCreateState() {
        return OrderCreateState.builder()
                .buyerId(1L)
                .amount(createOrderAmount())
                .shippingAddress(createShippingAddress())
                .orderProductStates(List.of(
                        OrderProductCreateState.builder()
                                .sellerId(10L)
                                .pricePolicyId(1L)
                                .quantity(1)
                                .unitAmount(10000L)
                                .accumulatedPoint(0L)
                                .build()
                ))
                .build();
    }

    private Order createOrderWithStatus(OrderStatus status) {
        return Order.from(OrderSnapshotState.builder()
                .id(1L)
                .buyerId(100L)
                .orderKey(UUID.randomUUID())
                .orderNumber("ORD-20260101-001")
                .orderStatus(status)
                .amount(createOrderAmount())
                .shippingAddress(createShippingAddress())
                .orderProductStates(List.of())
                .build());
    }

    private Order createOrderWithProducts(OrderStatus status) {
        return Order.from(OrderSnapshotState.builder()
                .id(1L)
                .buyerId(100L)
                .orderKey(UUID.randomUUID())
                .orderNumber("ORD-20260101-001")
                .orderStatus(status)
                .amount(createOrderAmount())
                .shippingAddress(createShippingAddress())
                .orderProductStates(List.of(
                        OrderProductSnapshotState.builder()
                                .orderId(1L).sellerId(10L).pricePolicyId(1L)
                                .quantity(1).unitAmount(10000L).accumulatedPoint(0L).orderStatus(status).build(),
                        OrderProductSnapshotState.builder()
                                .orderId(1L).sellerId(10L).pricePolicyId(2L)
                                .quantity(2).unitAmount(20000L).accumulatedPoint(0L).orderStatus(status).build()
                ))
                .build());
    }

    private Order createOrderWithDeliveredProducts() {
        return Order.from(OrderSnapshotState.builder()
                .id(1L)
                .buyerId(100L)
                .orderKey(UUID.randomUUID())
                .orderNumber("ORD-20260101-001")
                .orderStatus(OrderStatus.DELIVERED)
                .amount(createOrderAmount())
                .shippingAddress(createShippingAddress())
                .orderProductStates(List.of(
                        OrderProductSnapshotState.builder()
                                .orderId(1L).sellerId(10L).pricePolicyId(1L)
                                .quantity(1).unitAmount(10000L).accumulatedPoint(0L).orderStatus(OrderStatus.DELIVERED).build(),
                        OrderProductSnapshotState.builder()
                                .orderId(1L).sellerId(10L).pricePolicyId(2L)
                                .quantity(2).unitAmount(20000L).accumulatedPoint(0L).orderStatus(OrderStatus.DELIVERED).build()
                ))
                .build());
    }

    private Order createOrderWithReturnInProgressProducts() {
        return Order.from(OrderSnapshotState.builder()
                .id(1L)
                .buyerId(100L)
                .orderKey(UUID.randomUUID())
                .orderNumber("ORD-20260101-001")
                .orderStatus(OrderStatus.RETURN_IN_PROGRESS)
                .amount(createOrderAmount())
                .shippingAddress(createShippingAddress())
                .orderProductStates(List.of(
                        OrderProductSnapshotState.builder()
                                .orderId(1L).sellerId(10L).pricePolicyId(1L)
                                .quantity(1).unitAmount(10000L).accumulatedPoint(0L).orderStatus(OrderStatus.RETURN_IN_PROGRESS).build(),
                        OrderProductSnapshotState.builder()
                                .orderId(1L).sellerId(10L).pricePolicyId(2L)
                                .quantity(2).unitAmount(20000L).accumulatedPoint(0L).orderStatus(OrderStatus.RETURN_IN_PROGRESS).build()
                ))
                .build());
    }

    @SuppressWarnings("deprecation")
    private OrderAmount createOrderAmount() {
        return OrderAmount.of(50000L, 45000L, 3000L, 2000L, 0L);
    }

    private ShippingAddress createShippingAddress() {
        return ShippingAddress.of(
                "홍길동", "010-1234-5678", "06000", "서울시 강남구", "101동 202호",
                DeliveryRequestType.LEAVE_AT_DOOR, "문 앞에 놓아주세요"
        );
    }
}
