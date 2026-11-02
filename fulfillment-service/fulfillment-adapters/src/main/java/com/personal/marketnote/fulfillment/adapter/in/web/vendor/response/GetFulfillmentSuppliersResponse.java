package com.personal.marketnote.fulfillment.adapter.in.web.vendor.response;

import com.personal.marketnote.fulfillment.port.in.result.vendor.FulfillmentSupplierInfoResult;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentSuppliersResult;

import java.util.List;

public record GetFulfillmentSuppliersResponse(
        Integer dataCount,
        List<FulfillmentSupplierInfoResult> suppliers
) {
    public static GetFulfillmentSuppliersResponse from(GetFulfillmentSuppliersResult result) {
        return new GetFulfillmentSuppliersResponse(result.dataCount(), result.suppliers());
    }
}
