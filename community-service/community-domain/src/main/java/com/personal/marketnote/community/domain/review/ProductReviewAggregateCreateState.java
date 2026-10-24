package com.personal.marketnote.community.domain.review;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ProductReviewAggregateCreateState {
    private final Long productId;
    private final Integer totalCount;
    private final Integer fivePointCount;
    private final Integer fourPointCount;
    private final Integer threePointCount;
    private final Integer twoPointCount;
    private final Integer onePointCount;
    private final BigDecimal averageRating;
}
