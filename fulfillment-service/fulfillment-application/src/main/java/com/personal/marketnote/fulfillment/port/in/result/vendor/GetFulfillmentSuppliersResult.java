package com.personal.marketnote.fulfillment.port.in.result.vendor;

import java.util.List;

public record GetFulfillmentSuppliersResult(
        Integer dataCount,
        List<FulfillmentSupplierInfoResult> suppliers
) {
    public static GetFulfillmentSuppliersResult of(
            Integer dataCount,
            List<FulfillmentSupplierInfoResult> suppliers
    ) {
        return new GetFulfillmentSuppliersResult(dataCount, suppliers);
    }
}
