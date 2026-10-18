package com.personal.marketnote.fulfillment.adapter.in.event.saga.payload;

import java.util.List;

public record FulfillmentCancelSagaPayload(
        Long orderId,
        List<String> productKeys
) {
}
