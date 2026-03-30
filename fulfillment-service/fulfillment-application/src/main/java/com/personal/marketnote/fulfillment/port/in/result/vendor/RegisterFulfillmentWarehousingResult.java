package com.personal.marketnote.fulfillment.port.in.result.vendor;

import java.util.List;

public record RegisterFulfillmentWarehousingResult(
        Integer dataCount,
        List<RegisterFulfillmentWarehousingItemResult> warehousing
) {
    public static RegisterFulfillmentWarehousingResult of(
            Integer dataCount,
            List<RegisterFulfillmentWarehousingItemResult> warehousing
    ) {
        return new RegisterFulfillmentWarehousingResult(dataCount, warehousing);
    }
}
