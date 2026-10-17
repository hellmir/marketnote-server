package com.personal.marketnote.common.kafka.event;

public record PricePolicyDeletedEvent(
        Long productId,
        Long pricePolicyId
) {
}
