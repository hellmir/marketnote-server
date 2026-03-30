package com.personal.marketnote.fulfillment.port.in.result.vendor;

import java.util.List;

public record FulfillmentDeliveryOutOrdGoodsByOrdNoInfoResult(
        String ordNo,
        String invoiceNo,
        List<FulfillmentDeliveryOutOrdGoodsByOrdNoItemInfoResult> goods
) {
    public static FulfillmentDeliveryOutOrdGoodsByOrdNoInfoResult of(
            String ordNo,
            String invoiceNo,
            List<FulfillmentDeliveryOutOrdGoodsByOrdNoItemInfoResult> goods
    ) {
        return new FulfillmentDeliveryOutOrdGoodsByOrdNoInfoResult(ordNo, invoiceNo, goods);
    }
}
