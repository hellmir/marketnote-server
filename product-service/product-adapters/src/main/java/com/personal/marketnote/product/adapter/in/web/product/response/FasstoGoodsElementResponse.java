package com.personal.marketnote.product.adapter.in.web.product.response;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.product.port.in.result.fulfillment.FulfillmentVendorGoodsElementInfoResult;

import java.util.List;

public record FasstoGoodsElementResponse(
        String godCd,
        String cstGodCd,
        String godNm,
        String useYn,
        List<FasstoGoodsElementItemResponse> elementList
) {
    public static FasstoGoodsElementResponse from(FulfillmentVendorGoodsElementInfoResult result) {
        List<FasstoGoodsElementItemResponse> elementList = FormatValidator.hasValue(result.elementList())
                ? result.elementList().stream()
                .map(FasstoGoodsElementItemResponse::from)
                .toList()
                : List.of();

        return new FasstoGoodsElementResponse(
                result.goodsCode(),
                result.customerGoodsCode(),
                result.goodsName(),
                result.enabled(),
                elementList
        );
    }
}
