package com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FulfillmentGoodsElementResponse(
        String godCd,
        String cstGodCd,
        String godNm,
        String useYn,
        List<FulfillmentGoodsElementItemResponse> elementList
) {
}
