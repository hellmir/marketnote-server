package com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FulfillmentDeliveryDetailGoodsResponse(
        String ordDt,
        String whCd,
        String slipNo,
        String cstCd,
        String shopCd,
        String supCd,
        String godCd,
        String cstGodCd,
        String godNm,
        String orgGodCd,
        String godType,
        String godTypeNm,
        String distTermDt,
        Integer stockQty,
        Integer ordQty,
        Integer addGodOrdQty,
        Integer ordQtySum,
        String giftDiv,
        String addType,
        String emgrYn
) {
}
