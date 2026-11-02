package com.personal.marketnote.commerce.adapter.in.event.saga.payload;

public record ReturnLedgerCompensationSagaPayload(
        Long orderId,
        Long paymentAmount,
        String idempotencyKey
) {
}
