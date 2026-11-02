package com.personal.marketnote.common.kafka.event;

public record ReturnRejectedEvent(
        Long orderId,
        Long buyerId
) {
}
