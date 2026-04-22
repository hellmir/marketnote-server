package com.personal.marketnote.fulfillment.port.in.result;

public record CancelInternalFulfillmentDeliveryResult(
        Long orderId,
        boolean cancelled,
        String message
) {
}
