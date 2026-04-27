package com.personal.marketnote.common.kafka.event;

public record ShippingPolicyChangedEvent(
        Long sellerId,
        Long shippingFee,
        Long freeShippingThreshold,
        Long jejuSurcharge,
        Long islandSurcharge,
        ShippingPolicyChangeAction action
) {
}
