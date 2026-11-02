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
}

