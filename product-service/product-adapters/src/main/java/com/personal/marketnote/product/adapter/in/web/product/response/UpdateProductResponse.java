package com.personal.marketnote.product.adapter.in.web.product.response;

import com.personal.marketnote.product.port.in.result.product.UpdateProductResult;

import java.util.UUID;

public record UpdateProductResponse(
        Long id,
        UUID productKey
) {
    public static UpdateProductResponse from(UpdateProductResult result) {
        return new UpdateProductResponse(result.id(), result.productKey());
    }
}
