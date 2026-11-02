package com.personal.marketnote.commerce.domain.order;

import com.personal.marketnote.common.utility.FormatValidator;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class OrderStatusHistory {
    private Long id;
    private Long orderId;
    private OrderStatus orderStatus;
    private OrderStatusReasonCategory reasonCategory;
    private String reason;
    private LocalDateTime createdAt;

    public static OrderStatusHistory from(OrderStatusHistoryCreateState state) {
        String reason = FormatValidator.hasValue(state.getReason())
                ? state.getReason()
                : state.getOrderStatus().getDescription();

        return OrderStatusHistory.builder()
                .orderId(state.getOrderId())
                .orderStatus(state.getOrderStatus())
                .reasonCategory(state.getReasonCategory())
                .reason(reason)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static OrderStatusHistory from(OrderStatusHistorySnapshotState state) {
        return OrderStatusHistory.builder()
                .id(state.getId())
                .orderId(state.getOrderId())
                .orderStatus(state.getOrderStatus())
                .reasonCategory(state.getReasonCategory())
                .reason(state.getReason())
                .createdAt(state.getCreatedAt())
                .build();
    }
}

