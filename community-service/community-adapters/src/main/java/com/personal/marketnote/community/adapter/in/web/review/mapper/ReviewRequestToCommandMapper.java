package com.personal.marketnote.community.adapter.in.web.review.mapper;

import com.personal.marketnote.community.adapter.in.web.review.request.RegisterReviewRequest;
import com.personal.marketnote.community.adapter.in.web.review.request.UpdateReviewRequest;
import com.personal.marketnote.community.port.in.command.review.RegisterReviewCommand;
import com.personal.marketnote.community.port.in.command.review.UpdateReviewCommand;

public class ReviewRequestToCommandMapper {
    public static RegisterReviewCommand mapToCommand(RegisterReviewRequest request, Long userId) {
        return RegisterReviewCommand.builder()
                .reviewerId(userId)
                .orderId(request.getOrderId())
                .productId(request.getProductId())
                .pricePolicyId(request.getPricePolicyId())
                .productImageUrl(request.getProductImageUrl())
                .selectedOptions(request.getSelectedOptions())
                .quantity(request.getQuantity())
                .reviewerName(request.getReviewerName())
                .rating(request.getRating())
                .content(request.getContent())
                .isPhoto(request.getIsPhoto())
                .build();
    }

    public static UpdateReviewCommand mapToCommand(Long id, UpdateReviewRequest request, Long userId) {
        return UpdateReviewCommand.builder()
                .id(id)
                .reviewerId(userId)
                .rating(request.getRating())
                .content(request.getContent())
                .isPhoto(request.getIsPhoto())
                .build();
    }
}
