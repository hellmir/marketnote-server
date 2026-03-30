package com.personal.marketnote.fulfillment.port.in.result.vendor;

public record UpdateFulfillmentSupplierResult(
        String msg,
        String code,
        String supCd
) {
    public static UpdateFulfillmentSupplierResult of(String msg, String code, String supCd) {
        return new UpdateFulfillmentSupplierResult(msg, code, supCd);
    }
}
