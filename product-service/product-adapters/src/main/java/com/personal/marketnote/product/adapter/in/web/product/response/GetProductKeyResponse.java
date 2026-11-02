package com.personal.marketnote.product.adapter.in.web.product.response;

import com.personal.marketnote.product.port.in.result.product.GetProductKeyResult;

import java.util.UUID;

public record GetProductKeyResponse(
        UUID productKey
) {
    public static GetProductKeyResponse from(GetProductKeyResult result) {
        return new GetProductKeyResponse(result.productKey());
    }
}
