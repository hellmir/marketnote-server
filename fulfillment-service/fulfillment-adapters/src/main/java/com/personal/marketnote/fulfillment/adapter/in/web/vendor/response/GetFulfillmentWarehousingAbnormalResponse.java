package com.personal.marketnote.fulfillment.adapter.in.web.vendor.response;

import com.personal.marketnote.fulfillment.port.in.result.vendor.FulfillmentWarehousingAbnormalInfoResult;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentWarehousingAbnormalResult;

import java.util.List;

public record GetFulfillmentWarehousingAbnormalResponse(
        Integer dataCount,
        List<FulfillmentWarehousingAbnormalInfoResult> abnormals
) {
    public static GetFulfillmentWarehousingAbnormalResponse from(GetFulfillmentWarehousingAbnormalResult result) {
        return new GetFulfillmentWarehousingAbnormalResponse(result.dataCount(), result.abnormals());
    }
}
