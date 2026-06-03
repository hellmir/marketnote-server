package com.personal.marketnote.common.kafka.event;

import java.util.List;
import java.util.UUID;

public record OrderReturnedEvent(
        Long orderId,
        String orderKey,
        Long buyerId,
        Long returnAmount,
        Long paymentAmount,
        Long pointAmount,
        Long shippingFee,
        boolean isFullReturn,
        Long returnShippingFee,
        List<OrderProductItem> returnProducts
) {

    public record OrderProductItem(
            Long pricePolicyId,
            UUID sharerKey,
            Integer quantity,
            Long unitAmount
    ) {
    }
}
