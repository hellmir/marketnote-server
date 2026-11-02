package com.personal.marketnote.product.port.in.result.option;

import com.personal.marketnote.product.domain.option.ProductOptionCategory;

import java.util.List;
import java.util.stream.Collectors;

public record SelectableProductOptionCategoryItemResult(
        Long id,
        String name,
        Long orderNum,
        String status,
        List<SelectableProductOptionItemResult> options
) {
    public static SelectableProductOptionCategoryItemResult from(
            ProductOptionCategory category,
            List<Long> selectedOptionIds
    ) {
        return new SelectableProductOptionCategoryItemResult(
                category.getId(),
                category.getName(),
                category.getOrderNum(),
                category.getStatus().name(),
                category.getOptions().stream()
                        .map(option -> SelectableProductOptionItemResult.from(
                                option, selectedOptionIds
                        ))
                        .collect(Collectors.toList())
        );
    }
}
