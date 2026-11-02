package com.personal.marketnote.fulfillment.port.in.result.vendor;

import java.util.List;

public record GetFulfillmentDeliveryStatusesResult(
        Integer dataCount,
        List<FulfillmentDeliveryStatusInfoResult> deliveryStatuses
) {
    public static GetFulfillmentDeliveryStatusesResult of(
            Integer dataCount,
            List<FulfillmentDeliveryStatusInfoResult> deliveryStatuses
    ) {
        return new GetFulfillmentDeliveryStatusesResult(dataCount, deliveryStatuses);
    }
}
