package com.personal.marketnote.community.domain.review;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ReviewCreateState {
    private final Long reviewerId;
    private final Long orderId;
    private final Long productId;
    private final Long pricePolicyId;
    private final String productImageUrl;
    private final String selectedOptions;
    private final Integer quantity;
    private final String reviewerName;
    private final Float rating;
    private final String content;
    private final Boolean isPhoto;
}
