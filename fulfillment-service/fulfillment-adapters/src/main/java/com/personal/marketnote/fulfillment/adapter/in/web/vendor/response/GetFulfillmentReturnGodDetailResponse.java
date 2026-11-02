package com.personal.marketnote.fulfillment.adapter.in.web.vendor.response;

import com.personal.marketnote.fulfillment.port.in.result.vendor.FulfillmentReturnGodDetailInfoResult;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentReturnGodDetailResult;

import java.util.List;

public record GetFulfillmentReturnGodDetailResponse(
        Integer dataCount,
        List<FulfillmentReturnGodDetailInfoResult> returnGodInfos
) {
    public static GetFulfillmentReturnGodDetailResponse from(GetFulfillmentReturnGodDetailResult result) {
        return new GetFulfillmentReturnGodDetailResponse(result.dataCount(), result.returnGodInfos());
    }
}
