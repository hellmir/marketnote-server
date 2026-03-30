package com.personal.marketnote.fulfillment.adapter.in.web.vendor.response;

import com.personal.marketnote.fulfillment.port.in.result.vendor.RegisterFulfillmentShopResult;

public record RegisterFulfillmentShopResponse(
        RegisterFulfillmentShopResult shopInfo
) {
    public static RegisterFulfillmentShopResponse from(RegisterFulfillmentShopResult shopInfo) {
        return new RegisterFulfillmentShopResponse(shopInfo);
    }
}
