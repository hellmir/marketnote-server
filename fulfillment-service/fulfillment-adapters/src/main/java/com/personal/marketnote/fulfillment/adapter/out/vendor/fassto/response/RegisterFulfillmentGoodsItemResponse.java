package com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RegisterFulfillmentGoodsItemResponse(
        String msg,
        String code,
        String cstGodCd
) {
    private static final String SUCCESS_CODE = "200";

    public boolean isSuccess() {
        return SUCCESS_CODE.equals(code);
    }
}
