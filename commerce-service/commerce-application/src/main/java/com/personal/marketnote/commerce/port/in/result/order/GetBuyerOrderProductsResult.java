package com.personal.marketnote.commerce.port.in.result.order;

import java.util.List;

public record GetBuyerOrderProductsResult(
        List<GetBuyerOrderProductResult> orderProducts
) {
    public static GetBuyerOrderProductsResult of(List<GetBuyerOrderProductResult> orderProducts) {
        return new GetBuyerOrderProductsResult(orderProducts);
    }
}
