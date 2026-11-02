package com.personal.marketnote.product.port.in.result.fulfillment;

import java.util.List;

public record GetFulfillmentVendorGoodsResult(
        Integer dataCount,
        List<FulfillmentVendorGoodsInfoResult> goods
) {
    public static GetFulfillmentVendorGoodsResult of(
            Integer dataCount,
            List<FulfillmentVendorGoodsInfoResult> goods
    ) {
        return new GetFulfillmentVendorGoodsResult(dataCount, goods);
    }
}
