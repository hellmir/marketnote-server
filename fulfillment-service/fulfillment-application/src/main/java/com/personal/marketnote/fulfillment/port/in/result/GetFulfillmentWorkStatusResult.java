package com.personal.marketnote.fulfillment.port.in.result;

public record GetFulfillmentWorkStatusResult(
        Long orderId,
        String workStatus
) {
}
