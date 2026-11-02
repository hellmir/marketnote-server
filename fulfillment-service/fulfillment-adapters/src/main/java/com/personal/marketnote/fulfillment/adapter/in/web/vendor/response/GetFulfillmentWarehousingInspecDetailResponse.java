package com.personal.marketnote.fulfillment.adapter.in.web.vendor.response;

import com.personal.marketnote.fulfillment.port.in.result.vendor.FulfillmentWarehousingInspecDetailInfoResult;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentWarehousingInspecDetailResult;

import java.util.List;

public record GetFulfillmentWarehousingInspecDetailResponse(
        Integer dataCount,
        List<FulfillmentWarehousingInspecDetailInfoResult> details
) {
    public static GetFulfillmentWarehousingInspecDetailResponse from(GetFulfillmentWarehousingInspecDetailResult result) {
        return new GetFulfillmentWarehousingInspecDetailResponse(result.dataCount(), result.details());
    }
}
