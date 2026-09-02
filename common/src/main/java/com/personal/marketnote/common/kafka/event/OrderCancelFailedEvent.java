package com.personal.marketnote.common.kafka.event;

public record OrderCancelFailedEvent(
        Long orderId,
        Long buyerId
) {
}
