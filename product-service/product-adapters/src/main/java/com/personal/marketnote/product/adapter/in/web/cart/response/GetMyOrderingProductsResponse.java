package com.personal.marketnote.product.adapter.in.web.cart.response;

import com.personal.marketnote.product.port.in.result.cart.GetCartProductResult;
import com.personal.marketnote.product.port.in.result.product.GetMyOrderProductsResult;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class GetMyOrderingProductsResponse {
    private List<GetCartProductResult> orderingProducts;

    public static GetMyOrderingProductsResponse from(GetMyOrderProductsResult result) {
        return new GetMyOrderingProductsResponse(result.orderingProducts());
    }
}


