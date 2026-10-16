package com.personal.marketnote.product.adapter.out.persistence.productcatalog.repository;

import com.personal.marketnote.product.adapter.out.persistence.productcatalog.entity.ProductCatalogReadModelJpaEntity;
import com.personal.marketnote.product.domain.product.ProductSearchTarget;
import com.personal.marketnote.product.domain.product.ProductSortProperty;

import java.util.List;

public interface ProductCatalogReadModelQueryRepository {

    List<ProductCatalogReadModelJpaEntity> findCatalogItems(
            Long categoryId,
            List<Long> pricePolicyIds,
            Long cursor,
            int pageSize,
            boolean isAsc,
            ProductSortProperty sortProperty,
            ProductSearchTarget searchTarget,
            String searchKeyword
    );

    long countCatalogItems(
            Long categoryId,
            ProductSearchTarget searchTarget,
            String searchKeyword
    );
}
