package com.personal.marketnote.community.domain.review;

import com.personal.marketnote.community.domain.review.exception.InvalidRatingPointException;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class ProductReviewAggregate {
    private Long productId;
    private int totalCount;
    private int fivePointCount;
    private int fourPointCount;
    private int threePointCount;
    private int twoPointCount;
    private int onePointCount;
    private float totalRating;
    private float averageRating;
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
                .build();
        Float totalRating = review.getRating();
        productReviewAggregate.addPoint(totalRating.intValue());
        productReviewAggregate.computeRating(totalRating);

        return productReviewAggregate;
    }

    public void changePoint(Float previousRating, Float newRating) {
        reducePoint(previousRating.intValue());
        addPoint(newRating.intValue());
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

    public void computeRating(Float point) {
        totalRating += point;
        averageRating = totalRating / totalCount;
    }
}

