package com.personal.marketnote.fulfillment.port.in.result.vendor;

public record UpdateFulfillmentSupplierResult(
        String message,
        String code,
        String supplierCode
) {
    public static UpdateFulfillmentSupplierResult of(String message, String code, String supplierCode) {
        return new UpdateFulfillmentSupplierResult(message, code, supplierCode);
    }
}
