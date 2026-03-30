package com.personal.marketnote.fulfillment.adapter.in.web.vendor.response;

import com.personal.marketnote.fulfillment.port.in.result.vendor.FulfillmentDeliveryDetailInfoResult;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentDeliveryDetailResult;

import java.util.List;

public record GetFulfillmentDeliveryDetailResponse(
        Integer dataCount,
        List<FulfillmentDeliveryDetailInfoResult> deliveries
) {
    public static GetFulfillmentDeliveryDetailResponse from(GetFulfillmentDeliveryDetailResult result) {
        return new GetFulfillmentDeliveryDetailResponse(result.dataCount(), result.deliveries());
    }
}
