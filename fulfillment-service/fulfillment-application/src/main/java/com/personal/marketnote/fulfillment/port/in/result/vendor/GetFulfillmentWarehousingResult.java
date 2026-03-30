package com.personal.marketnote.fulfillment.port.in.result.vendor;

import java.util.List;

public record GetFulfillmentWarehousingResult(
        Integer dataCount,
        List<FulfillmentWarehousingInfoResult> warehousing
) {
    public static GetFulfillmentWarehousingResult of(
            Integer dataCount,
            List<FulfillmentWarehousingInfoResult> warehousing
    ) {
        return new GetFulfillmentWarehousingResult(dataCount, warehousing);
    }
}
