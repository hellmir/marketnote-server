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
        List<OrderProductItem> orderProducts
) {

    public record OrderProductItem(
            Long pricePolicyId,
            Long sharerId,
            Integer quantity,
            Long unitAmount
    ) {
    }
}
