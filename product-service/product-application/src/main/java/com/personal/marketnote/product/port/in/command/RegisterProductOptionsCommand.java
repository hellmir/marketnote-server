package com.personal.marketnote.product.port.in.command;

import java.util.List;

public record RegisterProductOptionsCommand(
        Long productId,
        String categoryName,
        List<OptionItem> options
) {
    public static RegisterProductOptionsCommand of(Long productId, String categoryName, List<OptionItem> options) {
        return new RegisterProductOptionsCommand(productId, categoryName, options);
    }

    public record OptionItem(
            String content
    ) {
    }
}
