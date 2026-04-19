package com.personal.marketnote.product.domain.option;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.product.domain.product.Product;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ProductOptionCategorySnapshotState {
    private final Long id;
    private final Product product;
    private final String name;
    private final List<ProductOption> options;
    private final Long orderNum;
    private final EntityStatus status;
}

