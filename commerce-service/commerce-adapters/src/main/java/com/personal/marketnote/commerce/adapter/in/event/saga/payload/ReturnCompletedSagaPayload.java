package com.personal.marketnote.commerce.adapter.in.event.saga.payload;

import java.util.List;

public record ReturnCompletedSagaPayload(
        Long orderId,
        String orderKey,
        Long buyerId,
        Long returnAmount,
        Long paymentAmount,
        Long pointAmount,
        Long shippingFee,
        boolean isFullReturn,
        Long returnShippingFee,
        List<SagaOrderProductItem> returnProducts,
        String returnId
) {
}
