package com.personal.marketnote.fulfillment.port.in.result.vendor;

public record RegisterFulfillmentDeliveryItemResult(
        String fulfillmentSlipNumber,
        String orderNumber,
        String message,
        String code,
        Object outOfStockGoodsDetail
) {
    public static RegisterFulfillmentDeliveryItemResult of(
            String fulfillmentSlipNumber,
            String orderNumber,
            String message,
            String code,
            Object outOfStockGoodsDetail
    ) {
        return new RegisterFulfillmentDeliveryItemResult(
                fulfillmentSlipNumber,
                orderNumber,
                message,
                code,
                outOfStockGoodsDetail
        );
    }
}
