package com.personal.marketnote.product.port.in.result.cart;

import com.personal.marketnote.product.domain.cart.CartProduct;

import java.util.List;
import java.util.Map;

public record GetMyCartProductsResult(
        List<GetCartProductResult> cartProducts
) {
    public static GetMyCartProductsResult from(List<CartProduct> userCartProducts, Map<Long, Integer> inventories) {
        return new GetMyCartProductsResult(
                userCartProducts.stream()
                        .map(cartProduct -> GetCartProductResult.from(
                                cartProduct, inventories.get(cartProduct.getPricePolicyId())
                        ))
                        .toList()
        );
    }
}
