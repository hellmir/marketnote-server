package com.personal.marketnote.product.port.in.result.category;

import com.personal.marketnote.product.domain.category.Category;

public record RegisterCategoryResult(
        Long id,
        Long parentCategoryId,
        String name
) {
    public static RegisterCategoryResult from(Category category) {
        return new RegisterCategoryResult(
                category.getId(),
                category.getParentCategoryId(),
                category.getName()
        );
    }
}
