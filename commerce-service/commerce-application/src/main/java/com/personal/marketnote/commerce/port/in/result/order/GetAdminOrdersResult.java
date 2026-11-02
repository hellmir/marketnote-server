package com.personal.marketnote.commerce.port.in.result.order;

import com.personal.marketnote.commerce.domain.order.Order;
import com.personal.marketnote.commerce.port.out.result.product.ProductInfoResult;

import java.util.List;
import java.util.Map;

public record GetAdminOrdersResult(
        List<GetOrderResult> orders
) {
    public static GetAdminOrdersResult from(
            List<Order> orders,
            Map<Long, ProductInfoResult> productInfoResultsByPricePolicyId
    ) {
        List<GetOrderResult> results = orders.stream()
                .map(order -> GetOrderResult.from(order, productInfoResultsByPricePolicyId))
                .toList();
        return new GetAdminOrdersResult(results);
    }

    public static GetAdminOrdersResult empty() {
        return new GetAdminOrdersResult(List.of());
    }
}
