package com.personal.marketnote.fulfillment.adapter.in.web.vendor.response;

import com.personal.marketnote.fulfillment.port.in.result.vendor.UpdateFulfillmentSupplierResult;

public record UpdateFulfillmentSupplierResponse(
        UpdateFulfillmentSupplierResult supplierInfo
) {
    public static UpdateFulfillmentSupplierResponse from(UpdateFulfillmentSupplierResult supplierInfo) {
        return new UpdateFulfillmentSupplierResponse(supplierInfo);
    }
}
