package com.personal.marketnote.fulfillment.port.in.result.vendor;

public record UpdateFulfillmentGoodsItemResult(
        String fulfillmentSlipNumber,
        String orderNumber,
        String message,
        String code,
        Object outOfStockGoodsDetail
) {
    public static UpdateFulfillmentGoodsItemResult of(
            String fulfillmentSlipNumber,
            String orderNumber,
            String message,
            String code,
            Object outOfStockGoodsDetail
    ) {
        return new UpdateFulfillmentGoodsItemResult(
                fulfillmentSlipNumber,
                orderNumber,
                message,
                code,
                outOfStockGoodsDetail
        );
    }
}
