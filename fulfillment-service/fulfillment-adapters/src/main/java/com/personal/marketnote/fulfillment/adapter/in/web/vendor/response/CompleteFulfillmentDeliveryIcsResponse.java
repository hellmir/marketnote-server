package com.personal.marketnote.fulfillment.adapter.in.web.vendor.response;

import com.personal.marketnote.fulfillment.port.in.result.vendor.CompleteFulfillmentDeliveryIcsItemResult;
import com.personal.marketnote.fulfillment.port.in.result.vendor.CompleteFulfillmentDeliveryIcsResult;

import java.util.List;

public record CompleteFulfillmentDeliveryIcsResponse(
        Integer dataCount,
        List<CompleteFulfillmentDeliveryIcsItemResult> completions
) {
    public static CompleteFulfillmentDeliveryIcsResponse from(CompleteFulfillmentDeliveryIcsResult result) {
        return new CompleteFulfillmentDeliveryIcsResponse(result.dataCount(), result.completions());
    }
}
