package com.personal.marketnote.product.port.out.productcatalog;

import java.math.BigDecimal;

public interface SaveProductCatalogReadModelPort {

    void upsertCatalog(
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
            Long popularity
    );

    void updateStockQuantity(Long pricePolicyId, Integer stockQuantity);

    void updateRepresentativeImageUrl(Long productId, String representativeImageUrl);

    void updateReviewAggregate(Long productId, Integer reviewCount, Float reviewAverageRating);

    void deleteByPricePolicyId(Long pricePolicyId);

    void deleteByProductId(Long productId);
}
