package com.personal.marketnote.fulfillment.port.in.result.vendor;

import java.util.List;

public record GetFulfillmentDeliveryOutOrdGoodsByOrdNoResult(
        Integer dataCount,
        List<FulfillmentDeliveryOutOrdGoodsByOrdNoInfoResult> goodsByOrdNo
) {
    public static GetFulfillmentDeliveryOutOrdGoodsByOrdNoResult of(
            Integer dataCount,
            List<FulfillmentDeliveryOutOrdGoodsByOrdNoInfoResult> goodsByOrdNo
    ) {
        return new GetFulfillmentDeliveryOutOrdGoodsByOrdNoResult(dataCount, goodsByOrdNo);
    }
}
