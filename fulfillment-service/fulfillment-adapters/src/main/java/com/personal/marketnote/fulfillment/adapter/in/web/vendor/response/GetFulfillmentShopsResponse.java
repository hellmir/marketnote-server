package com.personal.marketnote.fulfillment.adapter.in.web.vendor.response;

import com.personal.marketnote.fulfillment.port.in.result.vendor.FulfillmentShopInfoResult;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentShopsResult;

import java.util.List;

public record GetFulfillmentShopsResponse(
        Integer dataCount,
        List<FulfillmentShopInfoResult> shops
) {
    public static GetFulfillmentShopsResponse from(GetFulfillmentShopsResult result) {
        return new GetFulfillmentShopsResponse(result.dataCount(), result.shops());
    }
}
