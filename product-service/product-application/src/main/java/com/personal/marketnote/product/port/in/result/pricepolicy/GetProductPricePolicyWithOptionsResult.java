package com.personal.marketnote.product.port.in.result.pricepolicy;

import com.personal.marketnote.product.domain.pricepolicy.PricePolicy;
import lombok.AccessLevel;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Builder(access = AccessLevel.PRIVATE)
public record GetProductPricePolicyWithOptionsResult(
        Long id,
        Long price,
        Long discountPrice,
        Long accumulatedPoint,
        BigDecimal discountRate,
        List<Long> optionIds
) {
    public static GetProductPricePolicyWithOptionsResult from(PricePolicy pricePolicy) {
        return GetProductPricePolicyWithOptionsResult.builder()
                .id(pricePolicy.getId())
                .price(pricePolicy.getPrice().getValue())
                .discountPrice(pricePolicy.getDiscountPrice().getValue())
                .accumulatedPoint(pricePolicy.getAccumulatedPoint().getValue())
                .discountRate(pricePolicy.getDiscountRate())
                .optionIds(pricePolicy.getOptionIds())
                .build();
    }
}
