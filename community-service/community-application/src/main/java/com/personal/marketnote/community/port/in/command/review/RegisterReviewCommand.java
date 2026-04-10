package com.personal.marketnote.community.port.in.command.review;

import lombok.Builder;

@Builder
public record RegisterReviewCommand(
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
        Boolean isPhoto
) {
}
