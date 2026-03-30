package com.personal.marketnote.fulfillment.adapter.in.web.vendor.response;

import com.personal.marketnote.fulfillment.port.in.result.vendor.RegisterFulfillmentDeliveryItemResult;
import com.personal.marketnote.fulfillment.port.in.result.vendor.RegisterFulfillmentDeliveryResult;

import java.util.List;

public record RegisterFulfillmentReturnDeliveryResponse(
        Integer dataCount,
        List<RegisterFulfillmentDeliveryItemResult> deliveries
) {
    public static RegisterFulfillmentReturnDeliveryResponse from(RegisterFulfillmentDeliveryResult result) {
        return new RegisterFulfillmentReturnDeliveryResponse(result.dataCount(), result.deliveries());
    }
}
