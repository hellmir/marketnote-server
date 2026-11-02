package com.personal.marketnote.product.adapter.in.web.cart.response;

import com.personal.marketnote.product.port.in.result.cart.GetCartProductResult;
import com.personal.marketnote.product.port.in.result.cart.GetMyCartProductsResult;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class GetMyCartProductsResponse {
    private List<GetCartProductResult> cartProducts;

    public static GetMyCartProductsResponse from(GetMyCartProductsResult result) {
        return new GetMyCartProductsResponse(result.cartProducts());
    }
}


