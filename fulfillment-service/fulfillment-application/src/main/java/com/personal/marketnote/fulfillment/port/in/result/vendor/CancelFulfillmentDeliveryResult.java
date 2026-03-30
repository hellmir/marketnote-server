package com.personal.marketnote.fulfillment.port.in.result.vendor;

import java.util.List;

public record CancelFulfillmentDeliveryResult(
        Integer dataCount,
        List<CancelFulfillmentDeliveryItemResult> deliveries
) {
    public static CancelFulfillmentDeliveryResult of(
            Integer dataCount,
            List<CancelFulfillmentDeliveryItemResult> deliveries
    ) {
        return new CancelFulfillmentDeliveryResult(dataCount, deliveries);
    }
}
