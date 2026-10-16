package com.personal.marketnote.product.adapter.out.persistence.productcatalog.mapper;

import com.personal.marketnote.product.adapter.out.persistence.productcatalog.entity.ProductCatalogReadModelJpaEntity;
import com.personal.marketnote.product.port.in.result.product.ProductCatalogItemResult;

public class ProductCatalogReadModelJpaEntityToResultMapper {

    private ProductCatalogReadModelJpaEntityToResultMapper() {
    }

    public static ProductCatalogItemResult mapToResult(ProductCatalogReadModelJpaEntity entity) {
        return new ProductCatalogItemResult(
                entity.getPricePolicyId(),
                entity.getProductId(),
                entity.getSellerId(),
                entity.getPrimaryCategoryId(),
                entity.getName(),
                entity.getBrandName(),
                entity.getPrice(),
                entity.getDiscountPrice(),
                entity.getAccumulatedPoint(),
                entity.getAccumulatedPointRate(),
                entity.getOrderNum(),
                entity.getPopularity(),
                entity.getRepresentativeImageUrl(),
                entity.getStockQuantity(),
                entity.getReviewCount(),
                entity.getReviewAverageRating(),
                entity.getStatus().name()
        );
    }
}
