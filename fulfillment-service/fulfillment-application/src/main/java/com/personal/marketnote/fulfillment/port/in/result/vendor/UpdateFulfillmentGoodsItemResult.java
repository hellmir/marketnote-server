package com.personal.marketnote.fulfillment.port.in.result.vendor;

public record UpdateFulfillmentGoodsItemResult(
        String fmsSlipNo,
        String orderNo,
        String msg,
        String code,
        Object outOfStockGoodsDetail
) {
    public static UpdateFulfillmentGoodsItemResult of(
            String fmsSlipNo,
            String orderNo,
            String msg,
            String code,
            Object outOfStockGoodsDetail
    ) {
        return new UpdateFulfillmentGoodsItemResult(
                fmsSlipNo,
                orderNo,
                msg,
                code,
                outOfStockGoodsDetail
        );
    }
}
