package com.personal.marketnote.commerce.adapter.in.event.saga.payload;

public record ReturnLedgerSagaPayload(
        Long orderId,
        Long returnAmount,
        String idempotencyKey
) {
}
