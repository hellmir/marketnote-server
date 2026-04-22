package com.personal.marketnote.fulfillment.adapter.in.web.delivery.response;

import com.personal.marketnote.fulfillment.port.in.result.CancelInternalFulfillmentDeliveryResult;

public record CancelInternalFulfillmentDeliveryResponse(
        Long orderId,
        boolean cancelled,
        String message
) {
    public static CancelInternalFulfillmentDeliveryResponse from(CancelInternalFulfillmentDeliveryResult result) {
        return new CancelInternalFulfillmentDeliveryResponse(
                result.orderId(),
                result.cancelled(),
                result.message()
        );
    }
}
