package com.personal.marketnote.common.kafka.event;

import java.math.BigDecimal;

public record ReviewUpdatedEvent(
        Long reviewId,
        Long productId,
        Integer totalCount,
        BigDecimal averageRating
) {
}
