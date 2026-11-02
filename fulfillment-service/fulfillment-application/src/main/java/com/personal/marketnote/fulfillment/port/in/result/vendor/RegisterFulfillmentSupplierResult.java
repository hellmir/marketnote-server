package com.personal.marketnote.fulfillment.port.in.result.vendor;

public record RegisterFulfillmentSupplierResult(
        String message,
        String code,
        String supplierCode
) {
    public static RegisterFulfillmentSupplierResult of(String message, String code, String supplierCode) {
        return new RegisterFulfillmentSupplierResult(message, code, supplierCode);
    }
}
