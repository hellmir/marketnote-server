package com.personal.marketnote.common.kafka.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ReviewUpdatedEvent(
    Long reviewId,
    Long productId,
    Integer totalCount,
    BigDecimal averageRating
) {
}
