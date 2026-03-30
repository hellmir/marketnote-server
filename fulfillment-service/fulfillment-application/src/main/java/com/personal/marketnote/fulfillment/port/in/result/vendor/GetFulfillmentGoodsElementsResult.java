package com.personal.marketnote.fulfillment.port.in.result.vendor;

import java.util.List;

public record GetFulfillmentGoodsElementsResult(
        Integer dataCount,
        List<FulfillmentGoodsElementInfoResult> elements
) {
    public static GetFulfillmentGoodsElementsResult of(
            Integer dataCount,
            List<FulfillmentGoodsElementInfoResult> elements
    ) {
        return new GetFulfillmentGoodsElementsResult(dataCount, elements);
    }
}
