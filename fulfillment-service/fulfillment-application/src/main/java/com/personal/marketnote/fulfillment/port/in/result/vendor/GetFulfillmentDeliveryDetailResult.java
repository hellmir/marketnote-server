package com.personal.marketnote.fulfillment.port.in.result.vendor;

import java.util.List;

public record GetFulfillmentDeliveryDetailResult(
        Integer dataCount,
        List<FulfillmentDeliveryDetailInfoResult> deliveries
) {
    public static GetFulfillmentDeliveryDetailResult of(
            Integer dataCount,
            List<FulfillmentDeliveryDetailInfoResult> deliveries
    ) {
        return new GetFulfillmentDeliveryDetailResult(dataCount, deliveries);
    }
}
