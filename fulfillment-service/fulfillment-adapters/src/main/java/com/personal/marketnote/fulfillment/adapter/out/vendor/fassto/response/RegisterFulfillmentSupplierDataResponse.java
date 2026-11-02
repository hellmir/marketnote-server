package com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RegisterFulfillmentSupplierDataResponse(
        String msg,
        String code,
        String supCd
) {
    private static final String SUCCESS_CODE = "200";

    public boolean isSuccess() {
        return SUCCESS_CODE.equals(code);
    }
}
