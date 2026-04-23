package com.personal.marketnote.commerce.port.out.fulfillment;

public record CancelFulfillmentReleaseResult(
        Long orderId,
        boolean cancelled,
        String message
) {
}
