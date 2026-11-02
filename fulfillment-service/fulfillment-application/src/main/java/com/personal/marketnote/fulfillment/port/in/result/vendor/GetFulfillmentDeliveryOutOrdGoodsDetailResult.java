package com.personal.marketnote.fulfillment.port.in.result.vendor;

import java.util.List;

public record GetFulfillmentDeliveryOutOrdGoodsDetailResult(
        Integer dataCount,
        List<FulfillmentDeliveryOutOrdGoodsInfoResult> goodsByInvoice
) {
    public static GetFulfillmentDeliveryOutOrdGoodsDetailResult of(
            Integer dataCount,
            List<FulfillmentDeliveryOutOrdGoodsInfoResult> goodsByInvoice
    ) {
        return new GetFulfillmentDeliveryOutOrdGoodsDetailResult(dataCount, goodsByInvoice);
    }
}
