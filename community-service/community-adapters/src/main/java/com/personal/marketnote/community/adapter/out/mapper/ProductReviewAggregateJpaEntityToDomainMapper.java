package com.personal.marketnote.community.adapter.out.mapper;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.community.adapter.out.persistence.review.entity.ProductReviewAggregateJpaEntity;
import com.personal.marketnote.community.domain.review.ProductReviewAggregate;
import com.personal.marketnote.community.domain.review.ProductReviewAggregateSnapshotState;

import java.util.Optional;

public class ProductReviewAggregateJpaEntityToDomainMapper {
    public static Optional<ProductReviewAggregate> mapToDomain(ProductReviewAggregateJpaEntity entity) {
        if (FormatValidator.hasNoValue(entity)) {
            return Optional.empty();
        }

        return Optional.of(
                ProductReviewAggregate.from(
                        ProductReviewAggregateSnapshotState.builder()
                                .productId(entity.getProductId())
                                .totalCount(entity.getTotalCount())
                                .fivePointCount(entity.getFivePointCount())
                                .fourPointCount(entity.getFourPointCount())
                                .threePointCount(entity.getThreePointCount())
                                .twoPointCount(entity.getTwoPointCount())
                                .onePointCount(entity.getOnePointCount())
                                .totalRating(entity.getTotalRating())
                                .averageRating(entity.getAverageRating())
                                .createdAt(entity.getCreatedAt())
                                .modifiedAt(entity.getModifiedAt())
                                .build()
                )
        );
    }
}

