package com.personal.marketnote.fulfillment.adapter.in.web.vendor.response;

import com.personal.marketnote.fulfillment.port.in.result.vendor.CancelFulfillmentDeliveryItemResult;
import com.personal.marketnote.fulfillment.port.in.result.vendor.CancelFulfillmentDeliveryResult;

import java.util.List;

public record CancelFulfillmentDeliveryResponse(
        Integer dataCount,
        List<CancelFulfillmentDeliveryItemResult> deliveries
) {
    public static CancelFulfillmentDeliveryResponse from(CancelFulfillmentDeliveryResult result) {
        return new CancelFulfillmentDeliveryResponse(result.dataCount(), result.deliveries());
    }
}
