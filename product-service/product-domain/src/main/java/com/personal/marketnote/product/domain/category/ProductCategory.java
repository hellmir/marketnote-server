package com.personal.marketnote.product.domain.category;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class ProductCategory {
    private Long productId;
    private Long categoryId;

    public static ProductCategory of(Long productId, Long categoryId) {
        return ProductCategory.builder()
                .productId(productId)
                .categoryId(categoryId)
                .build();
    }
}
