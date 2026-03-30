package com.personal.marketnote.fulfillment.port.in.result.vendor;

import java.util.List;

public record GetFulfillmentWarehousingAbnormalResult(
        Integer dataCount,
        List<FulfillmentWarehousingAbnormalInfoResult> abnormals
) {
    public static GetFulfillmentWarehousingAbnormalResult of(
            Integer dataCount,
            List<FulfillmentWarehousingAbnormalInfoResult> abnormals
    ) {
        return new GetFulfillmentWarehousingAbnormalResult(dataCount, abnormals);
    }
}
