package com.personal.marketnote.fulfillment.adapter.in.web.vendor.response;

import com.personal.marketnote.fulfillment.port.in.result.vendor.FulfillmentWarehousingInfoResult;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentWarehousingResult;

import java.util.List;

public record GetFulfillmentWarehousingResponse(
        Integer dataCount,
        List<FulfillmentWarehousingInfoResult> warehousing
) {
    public static GetFulfillmentWarehousingResponse from(GetFulfillmentWarehousingResult result) {
        return new GetFulfillmentWarehousingResponse(result.dataCount(), result.warehousing());
    }
}
