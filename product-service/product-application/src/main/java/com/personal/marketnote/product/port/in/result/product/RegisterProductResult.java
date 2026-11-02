package com.personal.marketnote.product.port.in.result.product;

import com.personal.marketnote.product.domain.product.Product;

import java.util.UUID;

public record RegisterProductResult(
        Long id,
        UUID productKey
) {
    public static RegisterProductResult from(Product product) {
        return new RegisterProductResult(product.getId(), product.getProductKey());
    }
}
