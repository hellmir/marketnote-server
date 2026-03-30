package com.personal.marketnote.fulfillment.adapter.in.web.vendor.response;

import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentWarehousingAbnormalImageResult;

public record GetFulfillmentWarehousingAbnormalImageResponse(
        Integer dataCount,
        Object data
) {
    public static GetFulfillmentWarehousingAbnormalImageResponse from(GetFulfillmentWarehousingAbnormalImageResult result) {
        return new GetFulfillmentWarehousingAbnormalImageResponse(result.dataCount(), result.data());
    }
}
