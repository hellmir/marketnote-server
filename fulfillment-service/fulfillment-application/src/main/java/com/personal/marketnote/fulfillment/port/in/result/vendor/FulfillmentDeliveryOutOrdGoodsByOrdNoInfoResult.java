package com.personal.marketnote.fulfillment.port.in.result.vendor;

import java.util.List;

public record FulfillmentDeliveryOutOrdGoodsByOrdNoInfoResult(
        String orderNumber,
        String invoiceNumber,
        List<FulfillmentDeliveryOutOrdGoodsByOrdNoItemInfoResult> goods
) {
    public static FulfillmentDeliveryOutOrdGoodsByOrdNoInfoResult of(
            String orderNumber,
            String invoiceNumber,
            List<FulfillmentDeliveryOutOrdGoodsByOrdNoItemInfoResult> goods
    ) {
        return new FulfillmentDeliveryOutOrdGoodsByOrdNoInfoResult(orderNumber, invoiceNumber, goods);
    }
}
