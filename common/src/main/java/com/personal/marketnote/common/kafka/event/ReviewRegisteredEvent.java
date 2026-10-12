package com.personal.marketnote.common.kafka.event;

public record ReviewRegisteredEvent(
        Long orderId,
        Long pricePolicyId,
        Long productId,
        Integer totalCount,
        Float averageRating,
        Long reviewerId,
        Long reviewId
) {
}
