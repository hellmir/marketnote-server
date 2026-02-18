package com.personal.marketnote.fulfillment.port.in.result.vendor;

import java.util.List;

public record CompleteFasstoDeliveryIcsResult(
        Integer dataCount,
        List<CompleteFasstoDeliveryIcsItemResult> completions
) {
    public static CompleteFasstoDeliveryIcsResult of(
            Integer dataCount,
            List<CompleteFasstoDeliveryIcsItemResult> completions
    ) {
        return new CompleteFasstoDeliveryIcsResult(dataCount, completions);
    }
}
