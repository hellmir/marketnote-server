package com.personal.marketnote.product.port.in.result.product;

import java.math.BigDecimal;
import java.util.List;

public record ProductCatalogItemResult(
        Long pricePolicyId,
        Long productId,
        Long sellerId,
        Long primaryCategoryId,
        String name,
        String brandName,
        Long price,
        Long discountPrice,
        BigDecimal discountRate,
        Long accumulatedPoint,
        BigDecimal accumulatedPointRate,
        Long orderNum,
        Long popularity,
        String representativeImageUrl,
        List<String> resizedImageUrls,
        Integer stockQuantity,
        Integer reviewCount,
        Float reviewAverageRating,
        String status
) {
}
