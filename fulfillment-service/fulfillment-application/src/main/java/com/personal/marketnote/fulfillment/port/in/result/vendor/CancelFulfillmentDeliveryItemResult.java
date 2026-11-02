package com.personal.marketnote.fulfillment.port.in.result.vendor;

public record CancelFulfillmentDeliveryItemResult(
        String fulfillmentSlipNumber,
        String orderNumber,
        String message,
        String code,
        Object outOfStockGoodsDetail
) {
    public static CancelFulfillmentDeliveryItemResult of(
            String fulfillmentSlipNumber,
            String orderNumber,
            String message,
            String code,
            Object outOfStockGoodsDetail
    ) {
        return new CancelFulfillmentDeliveryItemResult(fulfillmentSlipNumber, orderNumber, message, code, outOfStockGoodsDetail);
    }
}
