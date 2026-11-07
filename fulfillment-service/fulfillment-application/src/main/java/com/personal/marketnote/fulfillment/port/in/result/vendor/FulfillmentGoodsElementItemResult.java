package com.personal.marketnote.fulfillment.port.in.result.vendor;

import lombok.Builder;

@Builder
public record FulfillmentGoodsElementItemResult(
        String productCode,
        String customerProductCode,
        String productBarcode,
        String productName,
        String productType,
        String productTypeName,
        Integer quantity
) {
}
