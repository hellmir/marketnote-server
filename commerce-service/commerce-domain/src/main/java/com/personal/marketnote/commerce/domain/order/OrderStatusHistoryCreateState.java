package com.personal.marketnote.commerce.domain.order;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class OrderStatusHistoryCreateState {
    private final Long orderId;
    private final OrderStatus orderStatus;
    private final OrderStatusReasonCategory reasonCategory;
    private final String reason;

    public static OrderStatusHistoryCreateState of(
            Long orderId, OrderStatus orderStatus, OrderStatusReasonCategory reasonCategory, String reason
    ) {
        return OrderStatusHistoryCreateState.builder()
                .orderId(orderId)
                .orderStatus(orderStatus)
                .reasonCategory(reasonCategory)
                .reason(reason)
                .build();
    }

    public static OrderStatusHistoryCreateState forStatusOnly(Long orderId, OrderStatus orderStatus) {
        return of(orderId, orderStatus, null, null);
    }
}

