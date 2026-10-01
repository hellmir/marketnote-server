package com.personal.marketnote.commerce.port.in.result.order;

import com.personal.marketnote.commerce.domain.order.Order;
import com.personal.marketnote.commerce.port.out.result.product.ProductInfoResult;

import java.util.List;
import java.util.Map;

public record GetBuyerOrdersResult(
        List<Order> orders,
        Map<Long, ProductInfoResult> orderedProducts
) {
    public static GetBuyerOrdersResult of(
            List<Order> orders, Map<Long, ProductInfoResult> productInfoResultsByPricePolicyId
    ) {
        return new GetBuyerOrdersResult(orders, productInfoResultsByPricePolicyId);
    }
}
