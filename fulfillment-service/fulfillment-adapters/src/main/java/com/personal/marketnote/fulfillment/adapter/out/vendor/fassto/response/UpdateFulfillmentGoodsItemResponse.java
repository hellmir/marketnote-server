package com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record UpdateFulfillmentGoodsItemResponse(
        String fmsSlipNo,
        String orderNo,
        String msg,
        String code,
        Object outOfStockGoodsDetail
) {
    private static final String SUCCESS_CODE = "200";

    public boolean isSuccess() {
        return SUCCESS_CODE.equals(code);
    }
}
