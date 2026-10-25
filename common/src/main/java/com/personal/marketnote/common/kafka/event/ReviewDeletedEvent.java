package com.personal.marketnote.common.kafka.event;

import java.math.BigDecimal;

public record ReviewDeletedEvent(
        Long reviewId,
        Long productId,
        Integer totalCount,
        BigDecimal averageRating,
        Long reviewerId
) {
}
