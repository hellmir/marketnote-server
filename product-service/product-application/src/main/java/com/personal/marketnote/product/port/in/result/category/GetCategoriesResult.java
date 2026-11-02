package com.personal.marketnote.product.port.in.result.category;

import com.personal.marketnote.product.domain.category.Category;

import java.util.List;
import java.util.stream.Collectors;

public record GetCategoriesResult(
        List<CategoryItemResult> categories
) {
    public static GetCategoriesResult from(List<Category> categories) {
        return new GetCategoriesResult(
                categories.stream()
                        .map(CategoryItemResult::from)
                        .collect(Collectors.toList())
        );
    }
}
