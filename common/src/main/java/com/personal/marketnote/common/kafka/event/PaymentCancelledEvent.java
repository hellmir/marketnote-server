package com.personal.marketnote.common.kafka.event;

import java.util.List;

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
            Long sharerId,
            Integer quantity,
            Long unitAmount
    ) {
    }
}
