package com.personal.marketnote.fulfillment.port.in.result.vendor;

import java.util.List;

public record RegisterFulfillmentGoodsResult(
        Integer dataCount,
        List<RegisterFulfillmentGoodsItemResult> goods
) {
    public static RegisterFulfillmentGoodsResult of(
            Integer dataCount,
            List<RegisterFulfillmentGoodsItemResult> goods
    ) {
        return new RegisterFulfillmentGoodsResult(dataCount, goods);
    }
}
