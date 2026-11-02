package com.personal.marketnote.product.adapter.in.web.product.response;

import com.personal.marketnote.common.adapter.in.response.CursorResponse;
import com.personal.marketnote.product.port.in.result.product.GetProductsResult;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.stream.Collectors;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class GetProductsResponse {
    private CursorResponse<ProductItemResponse> products;

    public static GetProductsResponse from(GetProductsResult result) {
        return new GetProductsResponse(
                new CursorResponse<>(
                        result.totalElements(),
                        result.hasNext(),
                        result.nextCursor(),
                        result.products().stream()
                                .map(ProductItemResponse::from)
                                .collect(Collectors.toList())
                )
        );
    }
}
