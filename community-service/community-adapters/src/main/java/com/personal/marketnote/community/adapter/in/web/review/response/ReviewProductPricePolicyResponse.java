package com.personal.marketnote.community.adapter.in.web.review.response;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.community.port.in.result.review.ReviewProductPricePolicyResult;

import java.math.BigDecimal;

public record ReviewProductPricePolicyResponse(
        Long id,
        Long price,
        Long discountPrice,
        BigDecimal discountRate,
        Long accumulatedPoint
) {
    public static ReviewProductPricePolicyResponse from(ReviewProductPricePolicyResult result) {
        if (FormatValidator.hasNoValue(result)) {
            return null;
        }

        return new ReviewProductPricePolicyResponse(
                result.id(),
                result.price(),
                result.discountPrice(),
                result.discountRate(),
                result.accumulatedPoint()
        );
    }
}
