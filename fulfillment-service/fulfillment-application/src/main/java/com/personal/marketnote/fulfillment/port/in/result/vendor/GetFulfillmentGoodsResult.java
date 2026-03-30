package com.personal.marketnote.fulfillment.port.in.result.vendor;

import java.util.List;

public record GetFulfillmentGoodsResult(
        Integer dataCount,
        List<FulfillmentGoodsInfoResult> goods
) {
    public static GetFulfillmentGoodsResult of(
            Integer dataCount,
            List<FulfillmentGoodsInfoResult> goods
    ) {
        return new GetFulfillmentGoodsResult(dataCount, goods);
    }
}
