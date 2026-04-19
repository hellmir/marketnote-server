package com.personal.marketnote.fulfillment.port.in.result.vendor;

public record FulfillmentDeliveryOutOrdGoodsItemInfoResult(
        String customerProductCode,
        String productName,
        Integer packingQuantity
) {
    public static FulfillmentDeliveryOutOrdGoodsItemInfoResult of(
            String customerProductCode,
            String productName,
            Integer packingQuantity
    ) {
        return new FulfillmentDeliveryOutOrdGoodsItemInfoResult(customerProductCode, productName, packingQuantity);
    }
}
