package com.personal.marketnote.fulfillment.port.in.result.vendor;

public record CancelFulfillmentDeliveryItemResult(
        String fmsSlipNo,
        String orderNo,
        String msg,
        String code,
        Object outOfStockGoodsDetail
) {
    public static CancelFulfillmentDeliveryItemResult of(
            String fmsSlipNo,
            String orderNo,
            String msg,
            String code,
            Object outOfStockGoodsDetail
    ) {
        return new CancelFulfillmentDeliveryItemResult(fmsSlipNo, orderNo, msg, code, outOfStockGoodsDetail);
    }
}
