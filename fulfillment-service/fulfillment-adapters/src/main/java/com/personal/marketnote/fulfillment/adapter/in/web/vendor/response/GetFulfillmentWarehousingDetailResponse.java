package com.personal.marketnote.fulfillment.adapter.in.web.vendor.response;

import com.personal.marketnote.fulfillment.port.in.result.vendor.FulfillmentWarehousingDetailInfoResult;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentWarehousingDetailResult;

import java.util.List;

public record GetFulfillmentWarehousingDetailResponse(
        Integer dataCount,
        List<FulfillmentWarehousingDetailInfoResult> details
) {
    public static GetFulfillmentWarehousingDetailResponse from(GetFulfillmentWarehousingDetailResult result) {
        return new GetFulfillmentWarehousingDetailResponse(result.dataCount(), result.details());
    }
}
