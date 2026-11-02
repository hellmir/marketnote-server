package com.personal.marketnote.commerce.domain.order;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class OrderStatusHistorySnapshotState {
    private final Long id;
    private final Long orderId;
    private final OrderStatus orderStatus;
    private final OrderStatusReasonCategory reasonCategory;
    private final String reason;
    private final LocalDateTime createdAt;
}

