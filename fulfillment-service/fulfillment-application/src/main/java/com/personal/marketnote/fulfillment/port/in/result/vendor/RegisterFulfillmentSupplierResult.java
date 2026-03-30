package com.personal.marketnote.fulfillment.port.in.result.vendor;

public record RegisterFulfillmentSupplierResult(
        String msg,
        String code,
        String supCd
) {
    public static RegisterFulfillmentSupplierResult of(String msg, String code, String supCd) {
        return new RegisterFulfillmentSupplierResult(msg, code, supCd);
    }
}
