package com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FulfillmentStockItemResponse(
        String whCd,
        String godCd,
        String cstGodCd,
        String godNm,
        String distTermDt,
        String distTermMgtYn,
        String godBarcd,
        Integer stockQty,
        Integer badStockQty,
        Integer canStockQty,
        String cstSupCd,
        String supNm,
        String giftDiv,
        List<Object> goodsSerialNo,
        String slipNo
) {
}
