package com.personal.marketnote.product.port.in.result.pricepolicy;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.product.domain.pricepolicy.PricePolicy;
import com.personal.marketnote.product.port.in.result.option.ProductOptionItemResult;
import lombok.AccessLevel;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Builder(access = AccessLevel.PRIVATE)
public record GetProductPricePolicyResult(
        Long id,
        Long price,
        Long discountPrice,
        BigDecimal discountRate,
        Long accumulatedPoint,
        List<ProductOptionItemResult> options
) {
    public static GetProductPricePolicyResult from(PricePolicy pricePolicy) {
        return GetProductPricePolicyResult.builder()
                .id(pricePolicy.getId())
                .price(pricePolicy.getPrice().getValue())
                .discountPrice(pricePolicy.getDiscountPrice().getValue())
                .discountRate(pricePolicy.getDiscountRate())
                .accumulatedPoint(pricePolicy.getAccumulatedPoint().getValue())
                .build();
    }

    public static GetProductPricePolicyResult fromCart(PricePolicy pricePolicy) {
        return GetProductPricePolicyResult.builder()
                .id(pricePolicy.getId())
                .price(pricePolicy.getPrice().getValue())
                .discountPrice(pricePolicy.getDiscountPrice().getValue())
                .discountRate(pricePolicy.getDiscountRate())
                .accumulatedPoint(pricePolicy.getAccumulatedPoint().getValue())
                .options(
                        FormatValidator.hasValue(pricePolicy.getProductOptions())
                                ? pricePolicy.getProductOptions()
                                .stream()
                                .map(ProductOptionItemResult::from)
                                .toList()
                                : null
                )
                .build();
    }
}
