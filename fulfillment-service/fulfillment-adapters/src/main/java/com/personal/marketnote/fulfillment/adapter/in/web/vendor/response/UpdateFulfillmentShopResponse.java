package com.personal.marketnote.fulfillment.adapter.in.web.vendor.response;

import com.personal.marketnote.fulfillment.port.in.result.vendor.UpdateFulfillmentShopResult;

public record UpdateFulfillmentShopResponse(
        UpdateFulfillmentShopResult shopInfo
) {
    public static UpdateFulfillmentShopResponse from(UpdateFulfillmentShopResult shopInfo) {
        return new UpdateFulfillmentShopResponse(shopInfo);
    }
}
