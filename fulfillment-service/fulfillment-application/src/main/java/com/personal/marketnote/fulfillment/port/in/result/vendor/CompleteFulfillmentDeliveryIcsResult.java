package com.personal.marketnote.fulfillment.port.in.result.vendor;

import java.util.List;

public record CompleteFulfillmentDeliveryIcsResult(
        Integer dataCount,
        List<CompleteFulfillmentDeliveryIcsItemResult> completions
) {
    public static CompleteFulfillmentDeliveryIcsResult of(
            Integer dataCount,
            List<CompleteFulfillmentDeliveryIcsItemResult> completions
    ) {
        return new CompleteFulfillmentDeliveryIcsResult(dataCount, completions);
    }
}
