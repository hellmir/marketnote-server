package com.personal.marketnote.product.port.in.result.fulfillment;

import lombok.Builder;

import java.util.List;

@Builder
public record FulfillmentVendorGoodsElementInfoResult(
        String goodsCode,
        String customerGoodsCode,
        String goodsName,
        String enabled,
        List<FulfillmentVendorGoodsElementItemResult> elementList
) {
}
