package com.personal.marketnote.fulfillment.adapter.in.web.vendor.response;

import com.personal.marketnote.fulfillment.port.in.result.vendor.FulfillmentGoodsInfoResult;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentGoodsResult;

import java.util.List;

public record GetFulfillmentGoodsResponse(
        Integer dataCount,
        List<FulfillmentGoodsInfoResult> goods
) {
    public static GetFulfillmentGoodsResponse from(GetFulfillmentGoodsResult result) {
        return new GetFulfillmentGoodsResponse(result.dataCount(), result.goods());
    }
}
