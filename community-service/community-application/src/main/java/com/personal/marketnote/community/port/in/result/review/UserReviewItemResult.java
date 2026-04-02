package com.personal.marketnote.community.port.in.result.review;

import com.personal.marketnote.common.application.file.port.in.result.GetFileResult;
import com.personal.marketnote.community.domain.review.Review;
import lombok.AccessLevel;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder(access = AccessLevel.PRIVATE)
public record UserReviewItemResult(
        Long id,
        Long reviewerId,
        Long orderId,
        Long productId,
        Long pricePolicyId,
        String productImageUrl,
        String selectedOptions,
        Integer quantity,
        String reviewerName,
        Float rating,
        String content,
        Boolean isPhoto,
        List<GetFileResult> images,
        Boolean isEdited,
        Integer likeCount,
        boolean isUserLiked,
        String status,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt,
        Long orderNum,
        ReviewProductInfoResult product
) {
    public static UserReviewItemResult from(Review review) {
        return from(review, null, null);
    }

    public static UserReviewItemResult from(Review review, List<GetFileResult> images) {
        return from(review, images, null);
    }

    public static UserReviewItemResult from(
            Review review,
            List<GetFileResult> images,
            ReviewProductInfoResult product
    ) {
        return UserReviewItemResult.builder()
                .id(review.getId())
                .reviewerId(review.getReviewerId())
                .orderId(review.getOrderId())
                .productId(review.getProductId())
                .pricePolicyId(review.getPricePolicyId())
                .productImageUrl(review.getProductImageUrl())
                .selectedOptions(review.getSelectedOptions())
                .quantity(review.getQuantity())
                .reviewerName(review.getReviewerName())
                .rating(review.getRating())
                .content(review.getContent())
                .isPhoto(review.getIsPhoto())
                .images(images)
                .isEdited(review.getIsEdited())
                .likeCount(review.getLikeCount())
                .isUserLiked(review.isUserLiked())
                .status(review.getStatus().name())
                .createdAt(review.getCreatedAt())
                .modifiedAt(review.getModifiedAt())
                .orderNum(review.getOrderNum())
                .product(product)
                .build();
    }
}
