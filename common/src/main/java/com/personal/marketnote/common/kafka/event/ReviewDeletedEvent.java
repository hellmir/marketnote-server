package com.personal.marketnote.common.kafka.event;

public record ReviewDeletedEvent(
        Long reviewId,
        Long productId,
        Integer totalCount,
        Float averageRating
) {
}
