package com.personal.marketnote.fulfillment.adapter.in.web.vendor.response;

import com.personal.marketnote.fulfillment.port.in.result.vendor.RegisterFasstoDeliveryItemResult;
import com.personal.marketnote.fulfillment.port.in.result.vendor.RegisterFasstoDeliveryResult;

import java.util.List;

public record RegisterFasstoDirectReturnDeliveryResponse(
        Integer dataCount,
        List<RegisterFasstoDeliveryItemResult> deliveries
) {
    public static RegisterFasstoDirectReturnDeliveryResponse from(RegisterFasstoDeliveryResult result) {
        return new RegisterFasstoDirectReturnDeliveryResponse(result.dataCount(), result.deliveries());
    }
}
