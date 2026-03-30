package com.personal.marketnote.fulfillment.port.in.result.vendor;

import java.util.List;

public record GetFulfillmentDeliveryGoodDetailResult(
        Integer dataCount,
        List<FulfillmentDeliveryGoodDetailInfoResult> goodDetails
) {
    public static GetFulfillmentDeliveryGoodDetailResult of(
            Integer dataCount,
            List<FulfillmentDeliveryGoodDetailInfoResult> goodDetails
    ) {
        return new GetFulfillmentDeliveryGoodDetailResult(dataCount, goodDetails);
    }
}
