package com.personal.marketnote.fulfillment.adapter.in.web.delivery.response;

import com.personal.marketnote.fulfillment.port.in.result.RegisterInternalReturnDeliveryResult;

public record RegisterInternalReturnDeliveryResponse(
        Long orderId,
        String returnSlipNumber,
        boolean registered,
        String message
) {
    public static RegisterInternalReturnDeliveryResponse from(RegisterInternalReturnDeliveryResult result) {
        return new RegisterInternalReturnDeliveryResponse(
                result.orderId(),
                result.returnSlipNumber(),
                result.registered(),
                result.message()
        );
    }
}
