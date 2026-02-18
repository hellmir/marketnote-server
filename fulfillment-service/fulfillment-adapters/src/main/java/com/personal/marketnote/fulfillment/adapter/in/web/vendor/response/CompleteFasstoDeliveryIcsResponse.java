package com.personal.marketnote.fulfillment.adapter.in.web.vendor.response;

import com.personal.marketnote.fulfillment.port.in.result.vendor.CompleteFasstoDeliveryIcsItemResult;
import com.personal.marketnote.fulfillment.port.in.result.vendor.CompleteFasstoDeliveryIcsResult;

import java.util.List;

public record CompleteFasstoDeliveryIcsResponse(
        Integer dataCount,
        List<CompleteFasstoDeliveryIcsItemResult> completions
) {
    public static CompleteFasstoDeliveryIcsResponse from(CompleteFasstoDeliveryIcsResult result) {
        return new CompleteFasstoDeliveryIcsResponse(result.dataCount(), result.completions());
    }
}
