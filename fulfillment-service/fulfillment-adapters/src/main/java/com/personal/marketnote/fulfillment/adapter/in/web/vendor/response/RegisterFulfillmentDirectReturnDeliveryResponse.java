package com.personal.marketnote.fulfillment.adapter.in.web.vendor.response;

import com.personal.marketnote.fulfillment.port.in.result.vendor.RegisterFulfillmentDeliveryItemResult;
import com.personal.marketnote.fulfillment.port.in.result.vendor.RegisterFulfillmentDeliveryResult;

import java.util.List;

public record RegisterFulfillmentDirectReturnDeliveryResponse(
        Integer dataCount,
        List<RegisterFulfillmentDeliveryItemResult> deliveries
) {
    public static RegisterFulfillmentDirectReturnDeliveryResponse from(RegisterFulfillmentDeliveryResult result) {
        return new RegisterFulfillmentDirectReturnDeliveryResponse(result.dataCount(), result.deliveries());
    }
}
