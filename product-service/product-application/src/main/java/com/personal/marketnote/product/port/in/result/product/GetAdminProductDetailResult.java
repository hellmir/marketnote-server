package com.personal.marketnote.product.port.in.result.product;

import com.personal.marketnote.product.port.in.result.fulfillment.FulfillmentVendorGoodsElementInfoResult;
import com.personal.marketnote.product.port.in.result.fulfillment.FulfillmentVendorGoodsInfoResult;

public record GetAdminProductDetailResult(
        GetProductInfoWithOptionsResult product,
        FulfillmentVendorGoodsInfoResult fulfillmentGoods,
        FulfillmentVendorGoodsElementInfoResult fulfillmentGoodsElement
) {
    public static GetAdminProductDetailResult of(
            GetProductInfoWithOptionsResult product,
            FulfillmentVendorGoodsInfoResult fulfillmentGoods,
            FulfillmentVendorGoodsElementInfoResult fulfillmentGoodsElement
    ) {
        return new GetAdminProductDetailResult(product, fulfillmentGoods, fulfillmentGoodsElement);
    }
}
