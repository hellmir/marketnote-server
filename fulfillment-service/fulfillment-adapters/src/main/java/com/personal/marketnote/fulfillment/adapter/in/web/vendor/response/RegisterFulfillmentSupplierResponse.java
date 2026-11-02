package com.personal.marketnote.fulfillment.adapter.in.web.vendor.response;

import com.personal.marketnote.fulfillment.port.in.result.vendor.RegisterFulfillmentSupplierResult;

public record RegisterFulfillmentSupplierResponse(
        RegisterFulfillmentSupplierResult supplierInfo
) {
    public static RegisterFulfillmentSupplierResponse from(RegisterFulfillmentSupplierResult supplierInfo) {
        return new RegisterFulfillmentSupplierResponse(supplierInfo);
    }
}
