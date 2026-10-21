package com.personal.marketnote.product.port.in.result.product;

import com.personal.marketnote.product.domain.product.ProductTag;

public record ProductTagItem(
        Long id,
        String name,
        Long orderNum
) {
    public static ProductTagItem from(ProductTag tag) {
        return new ProductTagItem(tag.getId(), tag.getName(), tag.getOrderNum());
    }
}
