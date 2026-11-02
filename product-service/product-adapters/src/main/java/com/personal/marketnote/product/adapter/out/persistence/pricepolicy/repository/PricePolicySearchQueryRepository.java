package com.personal.marketnote.product.adapter.out.persistence.pricepolicy.repository;

import com.personal.marketnote.product.adapter.out.persistence.pricepolicy.entity.PricePolicyJpaEntity;
import com.personal.marketnote.product.domain.product.ProductSearchTarget;
import com.personal.marketnote.product.domain.product.ProductSortProperty;

import java.util.List;

public interface PricePolicySearchQueryRepository {
    List<PricePolicyJpaEntity> findActivePricePoliciesByOffset(
            List<Long> pricePolicyIds,
            int page,
            int pageSize,
            boolean isAsc,
            ProductSortProperty sortProperty,
            ProductSearchTarget searchTarget,
            String searchKeyword,
            Long categoryId
    );
}
