package com.personal.marketnote.commerce.adapter.in.event.saga.payload;

public record ReturnPointRefundSagaPayload(
        Long orderId,
        Long buyerId,
        Long refundPointAmount
) {
}
