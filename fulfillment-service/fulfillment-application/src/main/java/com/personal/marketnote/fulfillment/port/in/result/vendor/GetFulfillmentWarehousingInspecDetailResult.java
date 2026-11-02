package com.personal.marketnote.fulfillment.port.in.result.vendor;

import java.util.List;

public record GetFulfillmentWarehousingInspecDetailResult(
        Integer dataCount,
        List<FulfillmentWarehousingInspecDetailInfoResult> details
) {
    public static GetFulfillmentWarehousingInspecDetailResult of(
            Integer dataCount,
            List<FulfillmentWarehousingInspecDetailInfoResult> details
    ) {
        return new GetFulfillmentWarehousingInspecDetailResult(dataCount, details);
    }
}
