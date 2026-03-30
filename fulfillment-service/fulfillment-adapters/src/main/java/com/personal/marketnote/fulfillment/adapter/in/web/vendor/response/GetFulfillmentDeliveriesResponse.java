package com.personal.marketnote.fulfillment.adapter.in.web.vendor.response;

import com.personal.marketnote.fulfillment.port.in.result.vendor.FulfillmentDeliveryInfoResult;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentDeliveriesResult;

import java.util.List;

public record GetFulfillmentDeliveriesResponse(
        Integer dataCount,
        List<FulfillmentDeliveryInfoResult> deliveries
) {
    public static GetFulfillmentDeliveriesResponse from(GetFulfillmentDeliveriesResult result) {
        return new GetFulfillmentDeliveriesResponse(result.dataCount(), result.deliveries());
    }
}
