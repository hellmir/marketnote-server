package com.personal.marketnote.commerce.domain.order;

import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
public enum OrderStatusFilter {
    SHIPPING(
            List.of(
                    OrderStatus.PREPARING,
                    OrderStatus.SHIPPING,
                    OrderStatus.REFUND_SHIPPING
            )
    ),
    DELIVERED(List.of(OrderStatus.DELIVERED)),
    CONFIRMED(List.of(OrderStatus.CONFIRMED)),
    CANCEL_EXCHANGE_REFUND(
            List.of(
                    OrderStatus.CANCEL_REQUESTED,
                    OrderStatus.CANCELLED,
                    OrderStatus.REFUND_REQUESTED,
                    OrderStatus.REFUND_RECALLING,
                    OrderStatus.REFUND_SHIPPING,
                    OrderStatus.PARTIALLY_REFUNDED,
                    OrderStatus.REFUNDED
            )
    ),
    ALL(Collections.emptyList());

    private final List<OrderStatus> statuses;

    public List<OrderStatus> toStatuses() {
        return statuses;
    }
}
