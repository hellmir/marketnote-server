package com.personal.marketnote.common.kafka.event;

public record PricePolicyCreatedEvent(
        Long productId,
        Long pricePolicyId
) {
}
