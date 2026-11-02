package com.personal.marketnote.product.port.in.result.option;

import com.personal.marketnote.product.domain.option.ProductOptionCategory;

import java.util.List;
import java.util.stream.Collectors;

public record ProductOptionCategoryItemResult(
        Long id,
        String name,
        Long orderNum,
        String status,
        List<ProductOptionItemResult> options
) {
    public static ProductOptionCategoryItemResult from(ProductOptionCategory category) {
        return new ProductOptionCategoryItemResult(
                category.getId(),
                category.getName(),
                category.getOrderNum(),
                category.getStatus().name(),
                category.getOptions().stream()
                        .map(ProductOptionItemResult::from)
                        .collect(Collectors.toList())
        );
    }
}
