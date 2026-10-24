package com.personal.marketnote.community.port.in.result.review;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.community.domain.review.ProductReviewAggregate;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class ProductReviewAggregateResult {
    private int totalCount;
    private int fivePointCount;
    private int fourPointCount;
    private int threePointCount;
    private int twoPointCount;
    private int onePointCount;
    private BigDecimal averageRating = new BigDecimal("0.00");

    public static ProductReviewAggregateResult from(
            ProductReviewAggregate productReviewAggregate
    ) {
        if (FormatValidator.hasNoValue(productReviewAggregate)) {
            return new ProductReviewAggregateResult();
        }

        return new ProductReviewAggregateResult(
                productReviewAggregate.getTotalCount(),
                productReviewAggregate.getFivePointCount(),
                productReviewAggregate.getFourPointCount(),
                productReviewAggregate.getThreePointCount(),
                productReviewAggregate.getTwoPointCount(),
                productReviewAggregate.getOnePointCount(),
                productReviewAggregate.getAverageRating()
        );
    }
}
