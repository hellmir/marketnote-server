package com.personal.marketnote.fulfillment.port.in.result.vendor;

public record FulfillmentDeliveryOutOrdGoodsByOrdNoItemInfoResult(
        String customerProductCode,
        String productName,
        Integer orderQuantity
) {
    public static FulfillmentDeliveryOutOrdGoodsByOrdNoItemInfoResult of(
            String customerProductCode,
            String productName,
            Integer orderQuantity
    ) {
        return new FulfillmentDeliveryOutOrdGoodsByOrdNoItemInfoResult(customerProductCode, productName, orderQuantity);
    }
}
