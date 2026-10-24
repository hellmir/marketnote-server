package com.personal.marketnote.fulfillment.adapter.in.event.saga.payload;

import java.util.List;

public record OrderCancelFulfillmentSagaPayload(
        Long orderId,
        String originalStatus,
        List<String> productKeys
) {
}
