package com.personal.marketnote.fulfillment.adapter.in.web.vendor.response;

import com.personal.marketnote.fulfillment.port.in.result.vendor.FulfillmentDeliveryOutOrdGoodsByOrdNoInfoResult;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentDeliveryOutOrdGoodsByOrdNoResult;

import java.util.List;

public record GetFulfillmentDeliveryOutOrdGoodsByOrdNoResponse(
        Integer dataCount,
        List<FulfillmentDeliveryOutOrdGoodsByOrdNoInfoResult> goodsByOrdNo
) {
    public static GetFulfillmentDeliveryOutOrdGoodsByOrdNoResponse from(GetFulfillmentDeliveryOutOrdGoodsByOrdNoResult result) {
        return new GetFulfillmentDeliveryOutOrdGoodsByOrdNoResponse(result.dataCount(), result.goodsByOrdNo());
    }
}
