package com.personal.marketnote.product.port.in.result.pricepolicy;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.product.domain.pricepolicy.PricePolicy;
import com.personal.marketnote.product.port.in.result.option.ProductOptionItemResult;
import com.personal.marketnote.product.port.in.result.product.ProductCatalogItemResult;
import com.personal.marketnote.product.port.in.result.product.ProductCatalogOptionSnapshot;
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
                .discountRate(pricePolicy.getDiscountRate().getValue())
                .accumulatedPoint(pricePolicy.getAccumulatedPoint().getValue())
                .build();
    }

    public static GetProductPricePolicyResult fromReadModel(ProductCatalogItemResult catalogItem) {
        return GetProductPricePolicyResult.builder()
                .id(catalogItem.pricePolicyId())
                .price(FormatValidator.hasValue(catalogItem.price()) ? catalogItem.price() : 0L)
                .discountPrice(FormatValidator.hasValue(catalogItem.discountPrice()) ? catalogItem.discountPrice() : 0L)
                .discountRate(FormatValidator.hasValue(catalogItem.discountRate())
                        ? catalogItem.discountRate() : BigDecimal.ZERO)
                .accumulatedPoint(FormatValidator.hasValue(catalogItem.accumulatedPoint())
                        ? catalogItem.accumulatedPoint() : 0L)
                .options(toOptionItems(catalogItem.selectedOptions()))
                .build();
    }

    private static List<ProductOptionItemResult> toOptionItems(List<ProductCatalogOptionSnapshot> snapshots) {
        if (FormatValidator.hasNoValue(snapshots)) {
            return List.of();
        }
        return snapshots.stream()
                .map(snapshot -> new ProductOptionItemResult(
                        snapshot.id(), snapshot.content(), EntityStatus.ACTIVE.name()
                ))
                .toList();
    }

    public static GetProductPricePolicyResult fromCart(PricePolicy pricePolicy) {
        return GetProductPricePolicyResult.builder()
                .id(pricePolicy.getId())
                .price(pricePolicy.getPrice().getValue())
                .discountPrice(pricePolicy.getDiscountPrice().getValue())
                .discountRate(pricePolicy.getDiscountRate().getValue())
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
