package com.personal.marketnote.commerce.adapter.in.event.saga.payload;

public record FulfillmentCancelSagaPayload(
        Long orderId,
        String originalStatus
) {
}
