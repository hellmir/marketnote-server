package com.personal.marketnote.product.port.in.result.product;

import java.math.BigDecimal;

public record ProductCatalogItemResult(
        Long pricePolicyId,
        Long productId,
        Long sellerId,
        Long primaryCategoryId,
        String name,
        String brandName,
        Long price,
        Long discountPrice,
        Long accumulatedPoint,
        BigDecimal accumulatedPointRate,
        Long orderNum,
        Long popularity,
        String representativeImageUrl,
        Integer stockQuantity,
        Integer reviewCount,
        Float reviewAverageRating,
        String status
) {
}
