package com.personal.marketnote.common.kafka.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PaymentFailedEvent(
    Long orderId,
    String orderKey,
    String resultCode,
    String resultMessage
) {
}
