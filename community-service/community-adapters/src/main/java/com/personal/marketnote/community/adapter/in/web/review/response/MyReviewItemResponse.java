package com.personal.marketnote.community.adapter.in.web.review.response;

import com.personal.marketnote.common.application.file.port.in.result.GetFileResult;
import com.personal.marketnote.community.port.in.result.review.MyReviewItemResult;
import lombok.AccessLevel;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder(access = AccessLevel.PRIVATE)
public record MyReviewItemResponse(
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
        ReviewProductInfoResponse product
) {
    public static MyReviewItemResponse from(MyReviewItemResult result) {
        return MyReviewItemResponse.builder()
                .id(result.id())
                .reviewerId(result.reviewerId())
                .orderId(result.orderId())
                .productId(result.productId())
                .pricePolicyId(result.pricePolicyId())
                .productImageUrl(result.productImageUrl())
                .selectedOptions(result.selectedOptions())
                .quantity(result.quantity())
                .reviewerName(result.reviewerName())
                .rating(result.rating())
                .content(result.content())
                .isPhoto(result.isPhoto())
                .images(result.images())
                .isEdited(result.isEdited())
                .likeCount(result.likeCount())
                .isUserLiked(result.isUserLiked())
                .status(result.status())
                .createdAt(result.createdAt())
                .modifiedAt(result.modifiedAt())
                .orderNum(result.orderNum())
                .product(ReviewProductInfoResponse.from(result.product()))
                .build();
    }
}
