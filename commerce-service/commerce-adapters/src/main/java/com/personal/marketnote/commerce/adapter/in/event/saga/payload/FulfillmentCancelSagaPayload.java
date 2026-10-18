package com.personal.marketnote.commerce.adapter.in.event.saga.payload;

import java.util.List;

public record FulfillmentCancelSagaPayload(
        Long orderId,
        String originalStatus,
        List<String> productKeys
) {
}
