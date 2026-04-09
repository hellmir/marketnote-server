package com.personal.marketnote.community.mapper;

import com.personal.marketnote.community.domain.review.ReviewCreateState;
import com.personal.marketnote.community.domain.review.ReviewVersionHistoryCreateState;
import com.personal.marketnote.community.port.in.command.review.RegisterReviewCommand;
import com.personal.marketnote.community.port.in.command.review.UpdateReviewCommand;

public class ReviewCommandToStateMapper {
    public static ReviewCreateState mapToState(RegisterReviewCommand command) {
        return ReviewCreateState.builder()
                .reviewerId(command.reviewerId())
                .orderId(command.orderId())
                .productId(command.productId())
                .pricePolicyId(command.pricePolicyId())
                .productImageUrl(command.productImageUrl())
                .selectedOptions(command.selectedOptions())
                .quantity(command.quantity())
                .reviewerName(command.reviewerName())
                .rating(command.rating())
                .content(command.content())
                .isPhoto(command.isPhoto())
                .build();
    }

    public static ReviewVersionHistoryCreateState mapToVersionHistoryState(Long reviewId, RegisterReviewCommand command) {
        return ReviewVersionHistoryCreateState.builder()
                .reviewId(reviewId)
                .rating(command.rating())
                .content(command.content())
                .isPhoto(command.isPhoto())
                .build();
    }

    public static ReviewVersionHistoryCreateState mapToVersionHistoryState(Long reviewId, UpdateReviewCommand command) {
        return ReviewVersionHistoryCreateState.builder()
                .reviewId(reviewId)
                .rating(command.rating())
                .content(command.content())
                .isPhoto(command.isPhoto())
                .build();
    }
}
