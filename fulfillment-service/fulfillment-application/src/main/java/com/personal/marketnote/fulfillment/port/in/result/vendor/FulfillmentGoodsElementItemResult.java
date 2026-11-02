package com.personal.marketnote.fulfillment.port.in.result.vendor;

public record FulfillmentGoodsElementItemResult(
        String productCode,
        String customerProductCode,
        String productBarcode,
        String productName,
        String productType,
        String productTypeName,
        Integer quantity
) {
    public static FulfillmentGoodsElementItemResult of(
            String productCode,
            String customerProductCode,
            String productBarcode,
            String productName,
            String productType,
            String productTypeName,
            Integer quantity
    ) {
        return new FulfillmentGoodsElementItemResult(
                productCode,
                customerProductCode,
                productBarcode,
                productName,
                productType,
                productTypeName,
                quantity
        );
    }
}
