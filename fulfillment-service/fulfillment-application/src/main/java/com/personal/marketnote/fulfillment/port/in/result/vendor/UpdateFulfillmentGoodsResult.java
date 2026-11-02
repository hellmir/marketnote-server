package com.personal.marketnote.fulfillment.port.in.result.vendor;

import java.util.List;

public record UpdateFulfillmentGoodsResult(
        Integer dataCount,
        List<UpdateFulfillmentGoodsItemResult> goods
) {
    public static UpdateFulfillmentGoodsResult of(
            Integer dataCount,
            List<UpdateFulfillmentGoodsItemResult> goods
    ) {
        return new UpdateFulfillmentGoodsResult(dataCount, goods);
    }
}
