package com.personal.marketnote.fulfillment.port.in.result;

import java.time.LocalDateTime;

public record GetShippingStatusResult(
        Long orderId,
        String shippingStatus,
        boolean cancellable,
        String trackingNumber,
        String carrierCode,
        LocalDateTime lastPolledAt
) {
}
