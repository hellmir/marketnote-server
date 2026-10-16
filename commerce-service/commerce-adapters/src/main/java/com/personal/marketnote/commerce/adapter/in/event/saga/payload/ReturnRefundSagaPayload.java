package com.personal.marketnote.commerce.adapter.in.event.saga.payload;

public record ReturnRefundSagaPayload(
        Long orderId,
        String orderKey,
        Long buyerId,
        Long returnAmount,
        Long paymentAmount,
        boolean isFullReturn,
        Long returnShippingFee
) {
}
