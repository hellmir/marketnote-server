package com.personal.marketnote.fulfillment.port.in.result.vendor;

import java.util.List;

public record FulfillmentDeliveryOutOrdGoodsInfoResult(
        String invoiceNo,
        List<FulfillmentDeliveryOutOrdGoodsItemInfoResult> goodsDeliveredList
) {
    public static FulfillmentDeliveryOutOrdGoodsInfoResult of(
            String invoiceNo,
            List<FulfillmentDeliveryOutOrdGoodsItemInfoResult> goodsDeliveredList
    ) {
        return new FulfillmentDeliveryOutOrdGoodsInfoResult(invoiceNo, goodsDeliveredList);
    }
}
