package com.personal.marketnote.common.kafka.event;

import java.time.LocalDateTime;

public record ShippingStatusChangedEvent(
        Long orderId,
        String shippingStatus,
        String trackingNumber,
        String carrierCode,
        LocalDateTime occurredAt
) {
}
