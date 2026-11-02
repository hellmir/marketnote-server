package com.personal.marketnote.product.port.in.result.product;

import com.personal.marketnote.product.domain.product.Product;

import java.util.UUID;

public record UpdateProductResult(
        Long id,
        UUID productKey
) {
    public static UpdateProductResult from(Product product) {
        return new UpdateProductResult(product.getId(), product.getProductKey());
    }
}
