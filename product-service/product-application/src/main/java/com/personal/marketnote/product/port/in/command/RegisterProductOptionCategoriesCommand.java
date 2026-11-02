package com.personal.marketnote.product.port.in.command;

import java.util.List;

public record RegisterProductOptionCategoriesCommand(
        Long productId,
        List<CategoryItem> optionCategories
) {
    public static RegisterProductOptionCategoriesCommand of(Long productId, List<CategoryItem> optionCategories) {
        return new RegisterProductOptionCategoriesCommand(productId, optionCategories);
    }

    public record CategoryItem(String name, List<OptionItem> options) {
    }

    public record OptionItem(String content, Long price, Long accumulatedPoint) {
    }
}
