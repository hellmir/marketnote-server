package com.personal.marketnote.product.port.in.result.fulfillment;

import lombok.Builder;

@Builder
public record FulfillmentVendorGoodsElementItemResult(
        String goodsCode,
        String customerGoodsCode,
        String goodsBarcode,
        String goodsName,
        String goodsType,
        String goodsTypeName,
        Integer quantity
) {
}
