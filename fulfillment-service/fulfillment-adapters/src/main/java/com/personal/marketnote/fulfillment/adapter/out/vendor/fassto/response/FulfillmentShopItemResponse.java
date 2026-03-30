package com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FulfillmentShopItemResponse(
        String shopCd,
        String shopNm,
        String cstShopCd,
        String cstCd,
        String cstNm,
        String shopTp,
        String dealStrDt,
        String dealEndDt,
        String zipNo,
        String addr1,
        String addr2,
        String ceoNm,
        String busNo,
        String telNo,
        String unloadWay,
        String checkWay,
        String standYn,
        String formType,
        String empNm,
        String empPosit,
        String empTelNo,
        String useYn
) {
}
