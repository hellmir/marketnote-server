package com.personal.marketnote.common.kafka.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OrderCancelFailedEvent(
    Long orderId,
    Long buyerId
) {
}
