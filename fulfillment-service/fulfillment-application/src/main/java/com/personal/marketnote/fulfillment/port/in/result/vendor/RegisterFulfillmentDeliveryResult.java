package com.personal.marketnote.fulfillment.port.in.result.vendor;

import java.util.List;

public record RegisterFulfillmentDeliveryResult(
        Integer dataCount,
        List<RegisterFulfillmentDeliveryItemResult> deliveries
) {
    public static RegisterFulfillmentDeliveryResult of(
            Integer dataCount,
            List<RegisterFulfillmentDeliveryItemResult> deliveries
    ) {
        return new RegisterFulfillmentDeliveryResult(dataCount, deliveries);
    }
}
