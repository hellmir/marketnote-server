package com.personal.marketnote.product.port.in.result.product;

import com.personal.marketnote.product.domain.product.Product;

import java.util.UUID;

public record GetProductKeyResult(
        UUID productKey
) {
    public static GetProductKeyResult from(Product product) {
        return new GetProductKeyResult(product.getProductKey());
    }
}
