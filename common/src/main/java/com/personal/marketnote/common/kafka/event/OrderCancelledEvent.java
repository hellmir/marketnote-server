package com.personal.marketnote.common.kafka.event;

import java.util.List;
import java.util.UUID;

public record OrderCancelledEvent(
        Long orderId,
        String orderKey,
        Long buyerId,
        Long cancelAmount,
        Long paymentAmount,
        Long pointAmount,
        Long shippingFee,
        boolean isFullCancel,
        Long alreadyRefunded,
        List<OrderProductItem> orderProducts,
        List<OrderProductItem> cancelProducts
) {

    public record OrderProductItem(
            Long pricePolicyId,
            UUID sharerKey,
            Integer quantity,
            Long unitAmount
    ) {
    }
}
