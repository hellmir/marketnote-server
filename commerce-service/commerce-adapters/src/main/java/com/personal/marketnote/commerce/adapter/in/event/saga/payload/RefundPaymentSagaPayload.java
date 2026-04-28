package com.personal.marketnote.commerce.adapter.in.event.saga.payload;

public record RefundPaymentSagaPayload(
        Long orderId,
        String orderKey,
        Long buyerId,
        Long cancelAmount,
        Long paymentAmount,
        Long pointAmount,
        Long shippingFee,
        boolean isFullCancel,
        Long alreadyRefunded
) {
}
