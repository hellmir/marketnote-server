package com.personal.marketnote.common.kafka.event;

public record ProductRegisteredEvent(
        Long productId,
        Long pricePolicyId,
        Long sellerId,
        String productName,
        String goodsType
) {
}
