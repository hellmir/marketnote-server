package com.personal.marketnote.common.kafka.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PaymentApprovedEvent(
    Long orderId,
    String orderKey,
    Long paymentAmount
) {
}
