package com.personal.marketnote.common.kafka.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
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
