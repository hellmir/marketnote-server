package com.personal.marketnote.fulfillment.adapter.in.event.saga.payload;

import java.util.List;

public record OrderCancelFulfillmentSagaPayload(
        Long orderId,
        Long buyerId,
        String originalStatus,
        List<String> productKeys
) {
}
