package com.personal.marketnote.product.port.in.result.product;

import com.personal.marketnote.product.port.in.result.cart.GetCartProductResult;

import java.util.List;

public record GetMyOrderProductsResult(
        List<GetCartProductResult> orderingProducts
) {
    public static GetMyOrderProductsResult from(List<GetCartProductResult> orderingProducts) {
        return new GetMyOrderProductsResult(orderingProducts);
    }
}
