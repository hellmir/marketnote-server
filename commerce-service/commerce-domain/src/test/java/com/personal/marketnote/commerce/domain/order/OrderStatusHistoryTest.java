package com.personal.marketnote.commerce.domain.order;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class OrderStatusHistoryTest {

    @Test
    @DisplayName("reason이 있으면 그대로 사용하여 OrderStatusHistory를 생성한다")
    void from_createState_withReason_usesGivenReason() {
        OrderStatusHistoryCreateState state = OrderStatusHistoryCreateState.builder()
                .orderId(1L)
                .orderStatus(OrderStatus.CANCELLED)
                .reasonCategory(OrderStatusReasonCategory.CANCEL_ORDER)
                .reason("단순 변심으로 인한 취소")
                .build();

        OrderStatusHistory history = OrderStatusHistory.from(state);

        assertThat(history.getOrderId()).isEqualTo(1L);
        assertThat(history.getOrderStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(history.getReasonCategory()).isEqualTo(OrderStatusReasonCategory.CANCEL_ORDER);
        assertThat(history.getReason()).isEqualTo("단순 변심으로 인한 취소");
    }

    @Test
    @DisplayName("reason이 null이면 orderStatus의 description으로 폴백하여 OrderStatusHistory를 생성한다")
    void from_createState_withNullReason_fallsBackToOrderStatusDescription() {
        OrderStatusHistoryCreateState state = OrderStatusHistoryCreateState.builder()
                .orderId(2L)
                .orderStatus(OrderStatus.CANCELLED)
                .reasonCategory(OrderStatusReasonCategory.CANCEL_ORDER)
                .reason(null)
                .build();

        OrderStatusHistory history = OrderStatusHistory.from(state);

        assertThat(history.getReason()).isEqualTo(OrderStatus.CANCELLED.getDescription());
    }

    @Test
    @DisplayName("SnapshotState로 OrderStatusHistory를 복원하면 모든 필드가 그대로 설정된다")
    void from_snapshotState_restoresAllFields() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 1, 0, 0, 0);
        OrderStatusHistorySnapshotState state = OrderStatusHistorySnapshotState.builder()
                .id(10L)
                .orderId(1L)
                .orderStatus(OrderStatus.PAID)
                .reasonCategory(OrderStatusReasonCategory.ETC)
                .reason("결제 완료")
                .createdAt(createdAt)
                .build();

        OrderStatusHistory history = OrderStatusHistory.from(state);

        assertThat(history.getId()).isEqualTo(10L);
        assertThat(history.getOrderId()).isEqualTo(1L);
        assertThat(history.getOrderStatus()).isEqualTo(OrderStatus.PAID);
        assertThat(history.getReasonCategory()).isEqualTo(OrderStatusReasonCategory.ETC);
        assertThat(history.getReason()).isEqualTo("결제 완료");
        assertThat(history.getCreatedAt()).isEqualTo(createdAt);
    }
}
