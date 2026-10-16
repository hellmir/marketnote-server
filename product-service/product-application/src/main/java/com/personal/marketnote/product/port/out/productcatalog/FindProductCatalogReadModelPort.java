package com.personal.marketnote.product.port.out.productcatalog;

import com.personal.marketnote.product.domain.product.ProductSearchTarget;
import com.personal.marketnote.product.domain.product.ProductSortProperty;
import com.personal.marketnote.product.port.in.result.product.ProductCatalogItemResult;

import java.util.List;

public interface FindProductCatalogReadModelPort {

    List<ProductCatalogItemResult> findCatalogItems(
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
