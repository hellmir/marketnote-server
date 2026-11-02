package com.personal.marketnote.commerce.port.in.command.order;

import com.personal.marketnote.common.utility.FormatValidator;

public record GetBuyerOrderProductsQuery(
        Long buyerId,
        Boolean isReviewed
) {
    public static GetBuyerOrderProductsQuery of(Long buyerId, Boolean isReviewed) {
        return new GetBuyerOrderProductsQuery(buyerId, isReviewed);
    }

    public boolean matchesReviewStatus(Boolean reviewStatus) {
        if (FormatValidator.hasNoValue(isReviewed)) {
            return true;
        }

        return Boolean.TRUE.equals(isReviewed)
                ? Boolean.TRUE.equals(reviewStatus)
                : !Boolean.TRUE.equals(reviewStatus);
    }
}
