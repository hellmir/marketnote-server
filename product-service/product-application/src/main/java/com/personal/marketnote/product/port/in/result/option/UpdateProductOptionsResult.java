package com.personal.marketnote.product.port.in.result.option;

import com.personal.marketnote.product.domain.option.ProductOption;
import com.personal.marketnote.product.domain.option.ProductOptionCategory;

import java.util.List;
import java.util.stream.Collectors;

public record UpdateProductOptionsResult(
        Long id,
        List<Long> optionIds
) {
    public static UpdateProductOptionsResult from(ProductOptionCategory productOptionCategory) {
        return new UpdateProductOptionsResult(
                productOptionCategory.getId(),
                productOptionCategory.getOptions().stream()
                        .map(ProductOption::getId)
                        .collect(Collectors.toList())
        );
    }
}
