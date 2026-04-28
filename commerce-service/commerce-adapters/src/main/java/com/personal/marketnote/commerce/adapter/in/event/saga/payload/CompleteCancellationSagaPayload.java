package com.personal.marketnote.commerce.adapter.in.event.saga.payload;

import java.util.List;
import java.util.UUID;

public record CompleteCancellationSagaPayload(
        Long orderId,
        String orderKey,
        Long buyerId,
        Long cancelAmount,
        Long paymentAmount,
        Long pointAmount,
        Long shippingFee,
        boolean isFullCancel,
        Long alreadyRefunded,
        String reasonCategory,
        String reason,
        List<OrderProductItem> orderProducts
) {
    public record OrderProductItem(
            Long pricePolicyId,
            UUID sharerKey,
            Integer quantity,
            Long unitAmount
    ) {
    }
}
