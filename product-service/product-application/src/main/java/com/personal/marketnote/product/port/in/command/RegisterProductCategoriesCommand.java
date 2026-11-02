package com.personal.marketnote.product.port.in.command;

import java.util.List;

public record RegisterProductCategoriesCommand(
        Long productId,
        List<Long> categoryIds
) {
    public static RegisterProductCategoriesCommand of(Long productId, List<Long> categoryIds) {
        return new RegisterProductCategoriesCommand(productId, categoryIds);
    }
}
