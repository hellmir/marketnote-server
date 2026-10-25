package com.personal.marketnote.common.kafka.event;

import java.math.BigDecimal;

public record ReviewRegisteredEvent(
        Long orderId,
        Long pricePolicyId,
        Long productId,
        Integer totalCount,
        BigDecimal averageRating,
        Long reviewerId,
        Long reviewId
) {
}
