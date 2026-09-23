package com.personal.marketnote.commerce.domain.order;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OrderProductTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 4, 7, 14, 0, 0);

    @Nested
    @DisplayName("주문 상품 키 생성/복원 (orderProductKey)")
    class OrderProductKeyTest {

        @Test
        @DisplayName("CreateState로부터 생성 시 orderProductKey가 UUID V7로 자동 생성된다")
        void shouldGenerateOrderProductKeyAsUuidV7FromCreateState() {
            // given
            OrderProductCreateState state = OrderProductCreateState.builder()
                    .sellerId(10L)
                    .pricePolicyId(100L)
                    .quantity(1)
                    .unitAmount(50000L)
                    .accumulatedPoint(0L)
                    .build();

            // when
            OrderProduct orderProduct = OrderProduct.from(state);

            // then
            UUID orderProductKey = orderProduct.getOrderProductKey();
            assertThat(orderProductKey).isNotNull();
            assertThat(orderProductKey.version()).isEqualTo(7);
        }

        @Test
        @DisplayName("동일 CreateState로 두 번 생성해도 서로 다른 orderProductKey가 생성된다")
        void shouldGenerateDifferentOrderProductKeysForEachCreation() {
            // given
            OrderProductCreateState state = OrderProductCreateState.builder()
                    .sellerId(10L)
                    .pricePolicyId(100L)
                    .quantity(1)
                    .unitAmount(50000L)
                    .accumulatedPoint(0L)
                    .build();

            // when
            OrderProduct first = OrderProduct.from(state);
            OrderProduct second = OrderProduct.from(state);

            // then
            assertThat(first.getOrderProductKey()).isNotEqualTo(second.getOrderProductKey());
        }

        @Test
        @DisplayName("SnapshotState로부터 복원 시 orderProductKey가 그대로 보존된다")
        void shouldPreserveOrderProductKeyFromSnapshotState() {
            // given
            UUID expectedKey = UUID.fromString("018f0000-0000-7000-8000-000000000001");
            OrderProductSnapshotState state = OrderProductSnapshotState.builder()
                    .orderId(1L)
                    .sellerId(10L)
                    .pricePolicyId(100L)
                    .orderProductKey(expectedKey)
                    .quantity(1)
                    .unitAmount(50000L)
                    .accumulatedPoint(0L)
                    .orderStatus(OrderStatus.PAYMENT_PENDING)
                    .build();

            // when
            OrderProduct orderProduct = OrderProduct.from(state);

            // then
            assertThat(orderProduct.getOrderProductKey()).isEqualTo(expectedKey);
        }
    }

    @Nested
    @DisplayName("배송 완료 일시 설정 (deliveredAt)")
    class DeliveredAtTest {

        @Test
        @DisplayName("SHIPPING에서 DELIVERED로 전이하면 deliveredAt이 현재 시각으로 설정된다")
        void shouldSetDeliveredAtWhenTransitionToDelivered() {
            // given
            OrderProduct orderProduct = createOrderProduct(OrderStatus.SHIPPING);

            // when
            orderProduct.changeOrderStatus(OrderStatus.DELIVERED, NOW);

            // then
            assertThat(orderProduct.getDeliveredAt()).isEqualTo(NOW);
        }

        @Test
        @DisplayName("DELIVERED에서 CONFIRMED로 전이해도 deliveredAt은 변경되지 않는다")
        void shouldNotChangeDeliveredAtWhenTransitionToConfirmed() {
            // given
            LocalDateTime deliveredTime = LocalDateTime.of(2026, 4, 1, 10, 0, 0);
            OrderProduct orderProduct = createOrderProductWithDeliveredAt(deliveredTime);

            // when
            orderProduct.changeOrderStatus(OrderStatus.CONFIRMED, NOW);

            // then
            assertThat(orderProduct.getDeliveredAt()).isEqualTo(deliveredTime);
        }

        @Test
        @DisplayName("SnapshotState에서 복원 시 deliveredAt이 보존된다")
        void shouldPreserveDeliveredAtFromSnapshotState() {
            // given
            LocalDateTime deliveredTime = LocalDateTime.of(2026, 4, 1, 10, 0, 0);
            OrderProductSnapshotState state = OrderProductSnapshotState.builder()
                    .orderId(1L)
                    .sellerId(10L)
                    .pricePolicyId(100L)
                    .quantity(1)
                    .unitAmount(50000L)
                    .accumulatedPoint(0L)
                    .orderStatus(OrderStatus.DELIVERED)
                    .deliveredAt(deliveredTime)
                    .build();

            // when
            OrderProduct orderProduct = OrderProduct.from(state);

            // then
            assertThat(orderProduct.getDeliveredAt()).isEqualTo(deliveredTime);
        }
    }

    private OrderProduct createOrderProduct(OrderStatus status) {
        return OrderProduct.from(OrderProductSnapshotState.builder()
                .orderId(1L)
                .sellerId(10L)
                .pricePolicyId(100L)
                .quantity(1)
                .unitAmount(50000L)
                .accumulatedPoint(0L)
                .orderStatus(status)
                .build());
    }

    private OrderProduct createOrderProductWithDeliveredAt(LocalDateTime deliveredAt) {
        return OrderProduct.from(OrderProductSnapshotState.builder()
                .orderId(1L)
                .sellerId(10L)
                .pricePolicyId(100L)
                .quantity(1)
                .unitAmount(50000L)
                .accumulatedPoint(0L)
                .orderStatus(OrderStatus.DELIVERED)
                .deliveredAt(deliveredAt)
                .build());
    }
}
