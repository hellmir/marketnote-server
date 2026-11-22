package com.personal.marketnote.common.kafka.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ShippingStatusChangedEvent(
        Long orderId,
        Long buyerId,
        String shippingStatus,
        String trackingNumber,
        String carrierCode,
        LocalDateTime occurredAt,
        String buyerPhoneNumber,
        String buyerName
) {
}
