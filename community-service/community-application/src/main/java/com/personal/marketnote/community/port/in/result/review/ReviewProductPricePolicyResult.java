package com.personal.marketnote.community.port.in.result.review;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.community.port.out.result.product.ProductPricePolicyInfoResult;

import java.math.BigDecimal;

public record ReviewProductPricePolicyResult(
        Long id,
        Long price,
        Long discountPrice,
        BigDecimal discountRate,
        Long accumulatedPoint
) {
    public static ReviewProductPricePolicyResult from(ProductPricePolicyInfoResult pricePolicy) {
        if (FormatValidator.hasNoValue(pricePolicy)) {
            return null;
        }

        return new ReviewProductPricePolicyResult(
                pricePolicy.id(),
                pricePolicy.price(),
                pricePolicy.discountPrice(),
                pricePolicy.discountRate(),
                pricePolicy.accumulatedPoint()
        );
    }
}
