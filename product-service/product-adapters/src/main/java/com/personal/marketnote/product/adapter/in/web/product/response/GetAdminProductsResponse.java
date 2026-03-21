package com.personal.marketnote.product.adapter.in.web.product.response;

import com.personal.marketnote.common.adapter.in.response.OffsetResponse;
import com.personal.marketnote.product.port.in.result.product.GetAdminProductsResult;

public record GetAdminProductsResponse(OffsetResponse<ProductItemResponse> products) {
    public static GetAdminProductsResponse from(GetAdminProductsResult result) {
        return new GetAdminProductsResponse(
                new OffsetResponse<>(
                        result.page(),
                        result.pageSize(),
                        result.totalElements(),
                        result.totalPages(),
                        result.products().stream()
                                .map(ProductItemResponse::from)
                                .toList()
                )
        );
    }
}
