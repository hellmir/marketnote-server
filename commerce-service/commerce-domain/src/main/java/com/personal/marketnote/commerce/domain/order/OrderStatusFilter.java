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
                    OrderStatus.RETURN_SHIPPING
            )
    ),
    DELIVERED(List.of(OrderStatus.DELIVERED)),
    CONFIRMED(List.of(OrderStatus.CONFIRMED)),
    CANCEL_RETURN(
            List.of(
                    OrderStatus.CANCELLED,
                    OrderStatus.RETURN_REQUESTED,
                    OrderStatus.RETURN_RECALLING,
                    OrderStatus.RETURN_SHIPPING,
                    OrderStatus.PARTIALLY_RETURNED,
                    OrderStatus.RETURNED
            )
    ),
    ALL(Collections.emptyList());

    private final List<OrderStatus> statuses;

    public List<OrderStatus> toStatuses() {
        return statuses;
    }
}
