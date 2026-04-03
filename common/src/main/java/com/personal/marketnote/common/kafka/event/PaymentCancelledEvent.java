package com.personal.marketnote.common.kafka.event;

import java.util.List;
import java.util.UUID;

public record PaymentCancelledEvent(
        Long orderId,
        String orderKey,
        Long buyerId,
        Long cancelAmount,
        Long paymentAmount,
        Long pointAmount,
        boolean isFullCancel,
        Long alreadyRefunded,
        String cancelId,
        List<OrderProductItem> orderProducts,
        List<OrderProductItem> cancelProducts,
        Long partialProductPendingDeduction
) {

    public record OrderProductItem(
            Long pricePolicyId,
            UUID sharerKey,
            Integer quantity,
            Long unitAmount
    ) {
    }
}
