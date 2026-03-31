package com.personal.marketnote.product.adapter.in.web.product.response;

import com.personal.marketnote.product.port.in.result.fulfillment.FulfillmentVendorGoodsElementItemResult;

public record FasstoGoodsElementItemResponse(
        String godCd,
        String cstGodCd,
        String godBarcd,
        String godNm,
        String godType,
        String godTypeNm,
        Integer qty
) {
    public static FasstoGoodsElementItemResponse from(FulfillmentVendorGoodsElementItemResult result) {
        return new FasstoGoodsElementItemResponse(
                result.goodsCode(),
                result.customerGoodsCode(),
                result.goodsBarcode(),
                result.goodsName(),
                result.goodsType(),
                result.goodsTypeName(),
                result.quantity()
        );
    }
}
