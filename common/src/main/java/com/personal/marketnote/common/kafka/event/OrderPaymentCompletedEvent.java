package com.personal.marketnote.common.kafka.event;

import java.util.List;

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
            Long sharerId,
            Integer quantity,
            Long unitAmount
    ) {
    }
}
