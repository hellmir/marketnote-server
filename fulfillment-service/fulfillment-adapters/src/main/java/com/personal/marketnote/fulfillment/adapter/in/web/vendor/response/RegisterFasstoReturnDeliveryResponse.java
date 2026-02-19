package com.personal.marketnote.fulfillment.adapter.in.web.vendor.response;

import com.personal.marketnote.fulfillment.port.in.result.vendor.RegisterFasstoDeliveryItemResult;
import com.personal.marketnote.fulfillment.port.in.result.vendor.RegisterFasstoDeliveryResult;

import java.util.List;

public record RegisterFasstoReturnDeliveryResponse(
        Integer dataCount,
        List<RegisterFasstoDeliveryItemResult> deliveries
) {
    public static RegisterFasstoReturnDeliveryResponse from(RegisterFasstoDeliveryResult result) {
        return new RegisterFasstoReturnDeliveryResponse(result.dataCount(), result.deliveries());
    }
}
