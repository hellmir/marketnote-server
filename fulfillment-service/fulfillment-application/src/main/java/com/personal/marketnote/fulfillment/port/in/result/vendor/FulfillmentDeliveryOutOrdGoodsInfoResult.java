package com.personal.marketnote.fulfillment.port.in.result.vendor;

import java.util.List;

public record FulfillmentDeliveryOutOrdGoodsInfoResult(
        String invoiceNumber,
        List<FulfillmentDeliveryOutOrdGoodsItemInfoResult> goodsDeliveredList
) {
    public static FulfillmentDeliveryOutOrdGoodsInfoResult of(
            String invoiceNumber,
            List<FulfillmentDeliveryOutOrdGoodsItemInfoResult> goodsDeliveredList
    ) {
        return new FulfillmentDeliveryOutOrdGoodsInfoResult(invoiceNumber, goodsDeliveredList);
    }
}
