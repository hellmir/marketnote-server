package com.personal.marketnote.common.kafka.event;

import java.time.LocalDateTime;

public record OrderAutoConfirmFailedEvent(
        Long orderId,
        Long buyerId,
        String reason,
        LocalDateTime failedAt
) {
}
