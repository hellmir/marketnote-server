package com.personal.marketnote.fulfillment.port.in.result.vendor;

import java.util.List;

public record GetFulfillmentShopsResult(
        Integer dataCount,
        List<FulfillmentShopInfoResult> shops
) {
    public static GetFulfillmentShopsResult of(
            Integer dataCount,
            List<FulfillmentShopInfoResult> shops
    ) {
        return new GetFulfillmentShopsResult(dataCount, shops);
    }
}
