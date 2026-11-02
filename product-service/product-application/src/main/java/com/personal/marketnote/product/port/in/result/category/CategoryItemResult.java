package com.personal.marketnote.product.port.in.result.category;

import com.personal.marketnote.product.domain.category.Category;

public record CategoryItemResult(
        Long id,
        Long parentCategoryId,
        String name,
        String status
) {
    public static CategoryItemResult from(Category category) {
        return new CategoryItemResult(
                category.getId(),
                category.getParentCategoryId(),
                category.getName(),
                category.getStatus().name()
        );
    }
}
