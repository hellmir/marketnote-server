package com.personal.marketnote.common.kafka.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OrderAutoConfirmFailedEvent(
        Long orderId,
        Long buyerId,
        String reason,
        LocalDateTime failedAt
) {
}
