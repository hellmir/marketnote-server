package com.personal.marketnote.community.adapter.out.mapper;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.community.adapter.out.persistence.review.entity.ReviewJpaEntity;
import com.personal.marketnote.community.domain.review.Review;
import com.personal.marketnote.community.domain.review.ReviewSnapshotState;

import java.util.Optional;

public class ReviewJpaEntityToDomainMapper {
    public static Optional<Review> mapToDomain(ReviewJpaEntity entity) {
        if (FormatValidator.hasNoValue(entity)) {
            return Optional.empty();
        }

        return Optional.of(
                Review.from(
                        ReviewSnapshotState.builder()
                                .id(entity.getId())
                                .reviewerId(entity.getReviewerId())
                                .orderId(entity.getOrderId())
                                .productId(entity.getProductId())
                                .pricePolicyId(entity.getPricePolicyId())
                                .productImageUrl(entity.getProductImageUrl())
                                .selectedOptions(entity.getSelectedOptions())
                                .quantity(entity.getQuantity())
                                .reviewerName(entity.getReviewerName())
                                .maskedReviewerName(entity.getMaskedReviewerName())
                                .rating(entity.getRating())
                                .content(entity.getContent())
                                .isPhoto(entity.getIsPhoto())
                                .isEdited(entity.getIsEdited())
                                .likeCount(entity.getLikeCount())
                                .status(entity.getStatus())
                                .createdAt(entity.getCreatedAt())
                                .modifiedAt(entity.getModifiedAt())
                                .orderNum(entity.getOrderNum())
                                .build()
                )
        );
    }
}
