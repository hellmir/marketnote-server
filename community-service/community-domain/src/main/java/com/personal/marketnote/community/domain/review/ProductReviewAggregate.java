package com.personal.marketnote.community.domain.review;

import com.personal.marketnote.community.domain.review.exception.InvalidRatingPointException;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class ProductReviewAggregate {
    private static final int AVERAGE_RATING_SCALE = 2;

    private Long productId;
    private int totalCount;
    private int fivePointCount;
    private int fourPointCount;
    private int threePointCount;
    private int twoPointCount;
    private int onePointCount;
    private long totalRating;
    private BigDecimal averageRating;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    public static ProductReviewAggregate from(ProductReviewAggregateSnapshotState state) {
        return ProductReviewAggregate.builder()
                .productId(state.getProductId())
                .totalCount(state.getTotalCount())
                .fivePointCount(state.getFivePointCount())
                .fourPointCount(state.getFourPointCount())
                .threePointCount(state.getThreePointCount())
                .twoPointCount(state.getTwoPointCount())
                .onePointCount(state.getOnePointCount())
                .totalRating(state.getTotalRating())
                .averageRating(state.getAverageRating())
                .createdAt(state.getCreatedAt())
                .modifiedAt(state.getModifiedAt())
                .build();
    }

    public static ProductReviewAggregate from(Review review) {
        ProductReviewAggregate productReviewAggregate = ProductReviewAggregate.builder()
                .productId(review.getProductId())
                .totalRating(0L)
                .averageRating(BigDecimal.ZERO)
                .build();
        Rating rating = review.getRating();
        productReviewAggregate.addPoint(rating.getValue());
        productReviewAggregate.computeRating(rating.getValue());

        return productReviewAggregate;
    }

    public void changePoint(Rating previousRating, Rating newRating) {
        reducePoint(previousRating.getValue());
        addPoint(newRating.getValue());
    }

    public void reducePoint(int point) {
        --totalCount;

        if (RatingPoint.isFive(point)) {
            --fivePointCount;
            return;
        }

        if (RatingPoint.isFour(point)) {
            --fourPointCount;
            return;
        }

        if (RatingPoint.isThree(point)) {
            --threePointCount;
            return;
        }

        if (RatingPoint.isTwo(point)) {
            --twoPointCount;
            return;
        }

        if (RatingPoint.isOne(point)) {
            --onePointCount;
            return;
        }

        throw new InvalidRatingPointException(point);
    }

    public void addPoint(int point) {
        ++totalCount;

        if (RatingPoint.isFive(point)) {
            ++fivePointCount;
            return;
        }

        if (RatingPoint.isFour(point)) {
            ++fourPointCount;
            return;
        }

        if (RatingPoint.isThree(point)) {
            ++threePointCount;
            return;
        }

        if (RatingPoint.isTwo(point)) {
            ++twoPointCount;
            return;
        }

        if (RatingPoint.isOne(point)) {
            ++onePointCount;
            return;
        }

        throw new InvalidRatingPointException(point);
    }

    public void computeRating(int point) {
        totalRating += point;
        if (totalCount == 0) {
            totalRating = 0L;
            averageRating = BigDecimal.ZERO;
            return;
        }
        averageRating = BigDecimal.valueOf(totalRating)
                .divide(BigDecimal.valueOf(totalCount), AVERAGE_RATING_SCALE, RoundingMode.HALF_UP);
    }
}
