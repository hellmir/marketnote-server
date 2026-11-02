package com.personal.marketnote.fulfillment.port.in.result.vendor;

import java.util.List;

public record GetFulfillmentDeliveriesResult(
        Integer dataCount,
        List<FulfillmentDeliveryInfoResult> deliveries
) {
    public static GetFulfillmentDeliveriesResult of(
            Integer dataCount,
            List<FulfillmentDeliveryInfoResult> deliveries
    ) {
        return new GetFulfillmentDeliveriesResult(dataCount, deliveries);
    }
}
