package com.personal.marketnote.fulfillment.adapter.in.web.vendor.response;

import com.personal.marketnote.fulfillment.port.in.result.vendor.FulfillmentDeliveryStatusInfoResult;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentDeliveryStatusesResult;

import java.util.List;

public record GetFulfillmentDeliveryStatusesResponse(
        Integer dataCount,
        List<FulfillmentDeliveryStatusInfoResult> deliveryStatuses
) {
    public static GetFulfillmentDeliveryStatusesResponse from(GetFulfillmentDeliveryStatusesResult result) {
        return new GetFulfillmentDeliveryStatusesResponse(result.dataCount(), result.deliveryStatuses());
    }
}
