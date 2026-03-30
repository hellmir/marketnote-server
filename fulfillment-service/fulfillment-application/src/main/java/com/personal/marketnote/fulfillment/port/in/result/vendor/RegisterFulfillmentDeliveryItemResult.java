package com.personal.marketnote.fulfillment.port.in.result.vendor;

public record RegisterFulfillmentDeliveryItemResult(
        String fmsSlipNo,
        String orderNo,
        String msg,
        String code,
        Object outOfStockGoodsDetail
) {
    public static RegisterFulfillmentDeliveryItemResult of(
            String fmsSlipNo,
            String orderNo,
            String msg,
            String code,
            Object outOfStockGoodsDetail
    ) {
        return new RegisterFulfillmentDeliveryItemResult(
                fmsSlipNo,
                orderNo,
                msg,
                code,
                outOfStockGoodsDetail
        );
    }
}
