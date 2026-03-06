package com.personal.marketnote.common.kafka.event;

public record ReviewRegisteredEvent(
        Long orderId,
        Long pricePolicyId
) {
}
