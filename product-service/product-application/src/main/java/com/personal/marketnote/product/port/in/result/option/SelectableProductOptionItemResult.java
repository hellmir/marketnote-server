package com.personal.marketnote.product.port.in.result.option;

import com.personal.marketnote.product.domain.option.ProductOption;

import java.util.List;

public record SelectableProductOptionItemResult(
        Long id,
        String content,
        String status,
        boolean isSelected
) {
    public static SelectableProductOptionItemResult from(ProductOption option, List<Long> ids) {
        return new SelectableProductOptionItemResult(
                option.getId(),
                option.getContent(),
                option.getStatus().name(),
                ids.contains(option.getId())
        );
    }
}
