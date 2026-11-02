package com.personal.marketnote.product.port.out.productcatalog;

import java.math.BigDecimal;
import java.util.List;

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
            BigDecimal discountRate,
            Long accumulatedPoint,
            BigDecimal accumulatedPointRate,
            Long orderNum,
            Long popularity,
            Long sales,
            String productTagsJson,
            String selectedOptionsJson
    );

    void increaseSales(Long pricePolicyId, Long delta);

    void updateProductTags(Long productId, String productTagsJson);

    void updateStockQuantity(Long pricePolicyId, Integer stockQuantity);

    void updateRepresentativeImage(Long productId, String representativeImageUrl, List<String> resizedImageUrls);

    void updateReviewAggregate(Long productId, Integer reviewCount, BigDecimal reviewAverageRating);

    void deleteByPricePolicyId(Long pricePolicyId);

    void deleteByProductId(Long productId);
}
