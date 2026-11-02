package com.personal.marketnote.commerce.adapter.in.web.order.response;

import com.personal.marketnote.commerce.port.in.result.order.GetBuyerOrderProductResult;
import com.personal.marketnote.commerce.port.in.result.order.GetBuyerOrderProductsResult;

import java.util.List;

public record GetMyOrderProductsResponse(
        List<GetBuyerOrderProductResult> orderProducts
) {
    public static GetMyOrderProductsResponse from(GetBuyerOrderProductsResult result) {
        return new GetMyOrderProductsResponse(result.orderProducts());
    }
}
