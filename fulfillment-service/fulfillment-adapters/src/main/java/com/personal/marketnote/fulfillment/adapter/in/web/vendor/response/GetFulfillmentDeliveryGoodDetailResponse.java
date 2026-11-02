package com.personal.marketnote.fulfillment.adapter.in.web.vendor.response;

import com.personal.marketnote.fulfillment.port.in.result.vendor.FulfillmentDeliveryGoodDetailInfoResult;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentDeliveryGoodDetailResult;

import java.util.List;

public record GetFulfillmentDeliveryGoodDetailResponse(
        Integer dataCount,
        List<FulfillmentDeliveryGoodDetailInfoResult> goodDetails
) {
    public static GetFulfillmentDeliveryGoodDetailResponse from(GetFulfillmentDeliveryGoodDetailResult result) {
        return new GetFulfillmentDeliveryGoodDetailResponse(result.dataCount(), result.goodDetails());
    }
}
