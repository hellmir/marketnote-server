package com.personal.marketnote.fulfillment.adapter.in.web.delivery.response;

import com.personal.marketnote.fulfillment.port.in.result.GetShippingStatusResult;

import java.time.LocalDateTime;

public record GetShippingStatusResponse(
        Long orderId,
        String shippingStatus,
        boolean cancellable,
        String trackingNumber,
        String carrierCode,
        LocalDateTime lastPolledAt
) {
    public static GetShippingStatusResponse from(GetShippingStatusResult result) {
        return new GetShippingStatusResponse(
                result.orderId(),
                result.shippingStatus(),
                result.cancellable(),
                result.trackingNumber(),
                result.carrierCode(),
                result.lastPolledAt()
        );
    }
}
