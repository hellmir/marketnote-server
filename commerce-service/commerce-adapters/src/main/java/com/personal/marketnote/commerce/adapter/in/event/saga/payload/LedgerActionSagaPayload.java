package com.personal.marketnote.commerce.adapter.in.event.saga.payload;

public record LedgerActionSagaPayload(
        Long orderId,
        Long paymentAmount
) {
}
