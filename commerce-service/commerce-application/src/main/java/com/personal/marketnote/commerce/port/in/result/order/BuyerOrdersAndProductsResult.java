package com.personal.marketnote.commerce.port.in.result.order;

import com.personal.marketnote.commerce.domain.order.Order;
import com.personal.marketnote.commerce.port.out.result.product.ProductInfoResult;
import com.personal.marketnote.common.utility.FormatValidator;

import java.util.List;
import java.util.Map;

public record BuyerOrdersAndProductsResult(
        List<Order> orders,
        Map<Long, ProductInfoResult> orderedProductsByPricePolicyId
) {
    public static BuyerOrdersAndProductsResult empty() {
        return new BuyerOrdersAndProductsResult(List.of(), Map.of());
    }

    public static BuyerOrdersAndProductsResult of(
            List<Order> orders,
            Map<Long, ProductInfoResult> orderedProductsByPricePolicyId
    ) {
        List<Order> resolvedOrders = FormatValidator.hasValue(orders)
                ? orders
                : List.of();
        Map<Long, ProductInfoResult> resolvedProducts = FormatValidator.hasValue(orderedProductsByPricePolicyId)
                ? orderedProductsByPricePolicyId
                : Map.of();

        return new BuyerOrdersAndProductsResult(resolvedOrders, resolvedProducts);
    }
}