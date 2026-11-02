package com.personal.marketnote.fulfillment.port.in.result.vendor;

import java.util.List;

public record UpdateFulfillmentWarehousingResult(
        Integer dataCount,
        List<UpdateFulfillmentWarehousingItemResult> warehousing
) {
    public static UpdateFulfillmentWarehousingResult of(
            Integer dataCount,
            List<UpdateFulfillmentWarehousingItemResult> warehousing
    ) {
        return new UpdateFulfillmentWarehousingResult(dataCount, warehousing);
    }
}
