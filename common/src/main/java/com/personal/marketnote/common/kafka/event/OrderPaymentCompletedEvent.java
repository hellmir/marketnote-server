package com.personal.marketnote.common.kafka.event;

import java.util.List;
import java.util.UUID;

public record OrderPaymentCompletedEvent(
        Long orderId,
        Long buyerId,
        Long totalAmount,
        Long pointAmount,
        List<OrderProductItem> orderProducts,
        Long totalAccumulatedPoint
) {

    public record OrderProductItem(
            Long pricePolicyId,
            UUID sharerKey,
            Integer quantity,
            Long unitAmount
    ) {
    }
}
