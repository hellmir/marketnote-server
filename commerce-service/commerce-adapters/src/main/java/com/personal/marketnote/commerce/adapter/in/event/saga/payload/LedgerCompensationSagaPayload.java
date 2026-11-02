package com.personal.marketnote.commerce.adapter.in.event.saga.payload;

public record LedgerCompensationSagaPayload(
        Long orderId,
        Long cancelAmount,
        String idempotencyKey
) {
}
