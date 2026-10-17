package com.personal.marketnote.common.kafka.event;

import java.util.UUID;

public record ProductRegisteredEvent(
        Long productId,
        UUID productKey,
        Long pricePolicyId,
        Long sellerId,
        String productName,
        String goodsType,
        String brandName,
        Long price,
        Long discountPrice,
        Long accumulatedPoint
) {
}
