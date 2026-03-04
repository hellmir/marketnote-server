package com.personal.marketnote.commerce.domain.order;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class OrderSnapshotState {
    private final Long id;
    private final Long buyerId;
    private final UUID orderKey;
    private final String orderNumber;
    private final OrderStatus orderStatus;
    private final OrderStatusReasonCategory statusChangeReasonCategory;
    private final String statusChangeReason;
    private final Long totalAmount;
    private final Long paidAmount;
    private final Long couponAmount;
    private final Long pointAmount;
    private final List<OrderProductSnapshotState> orderProductStates;
    private final LocalDateTime createdAt;
    private final LocalDateTime modifiedAt;
}

