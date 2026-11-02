package com.personal.marketnote.fulfillment.adapter.in.web.vendor.response;

import com.personal.marketnote.fulfillment.port.in.result.vendor.FulfillmentGoodsElementInfoResult;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentGoodsElementsResult;

import java.util.List;

public record GetFulfillmentGoodsElementsResponse(
        Integer dataCount,
        List<FulfillmentGoodsElementInfoResult> elements
) {
    public static GetFulfillmentGoodsElementsResponse from(GetFulfillmentGoodsElementsResult result) {
        return new GetFulfillmentGoodsElementsResponse(result.dataCount(), result.elements());
    }
}
