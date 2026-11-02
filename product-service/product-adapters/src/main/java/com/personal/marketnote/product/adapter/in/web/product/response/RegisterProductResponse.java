package com.personal.marketnote.product.adapter.in.web.product.response;

import com.personal.marketnote.product.port.in.result.product.RegisterProductResult;
import lombok.AccessLevel;
import lombok.Builder;

import java.util.UUID;

@Builder(access = AccessLevel.PRIVATE)
public record RegisterProductResponse(
        Long id,
        UUID productKey
) {
    public static RegisterProductResponse from(RegisterProductResult result) {
        return new RegisterProductResponse(result.id(), result.productKey());
    }
}
