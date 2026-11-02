package com.personal.marketnote.community.adapter.out.web.product.response;

import com.personal.marketnote.common.adapter.in.response.CursorResponse;

public record OrderedProductsResponse(
        CursorResponse<ProductsInfoResponse> products
) {
}
