package com.personal.marketnote.fulfillment.port.in.result.vendor;

import java.util.List;

public record GetFulfillmentWarehousingDetailResult(
        Integer dataCount,
        List<FulfillmentWarehousingDetailInfoResult> details
) {
    public static GetFulfillmentWarehousingDetailResult of(
            Integer dataCount,
            List<FulfillmentWarehousingDetailInfoResult> details
    ) {
        return new GetFulfillmentWarehousingDetailResult(dataCount, details);
    }
}
