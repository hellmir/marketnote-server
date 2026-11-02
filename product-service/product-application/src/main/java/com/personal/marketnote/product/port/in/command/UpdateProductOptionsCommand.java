package com.personal.marketnote.product.port.in.command;

import java.util.List;

public record UpdateProductOptionsCommand(
        Long productId,
        Long optionCategoryId,
        String categoryName,
        List<RegisterProductOptionsCommand.OptionItem> options
) {
    public static UpdateProductOptionsCommand of(
            Long productId,
            Long optionCategoryId,
            String categoryName,
            List<RegisterProductOptionsCommand.OptionItem> options
    ) {
        return new UpdateProductOptionsCommand(productId, optionCategoryId, categoryName, options);
    }
}
