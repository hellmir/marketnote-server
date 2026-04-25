package com.personal.marketnote.commerce.domain.order;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class OrderProductTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 4, 7, 14, 0, 0);

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
                .orderStatus(OrderStatus.DELIVERED)
                .deliveredAt(deliveredAt)
                .build());
    }
}
