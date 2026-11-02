package com.personal.marketnote.community.domain.review;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ProductReviewAggregateSnapshotState {
    private final Long productId;
    private final Integer totalCount;
    private final Integer fivePointCount;
    private final Integer fourPointCount;
    private final Integer threePointCount;
    private final Integer twoPointCount;
    private final Integer onePointCount;
    private final Long totalRating;
    private final BigDecimal averageRating;
    private final LocalDateTime createdAt;
    private final LocalDateTime modifiedAt;
}
