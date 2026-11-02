package com.personal.marketnote.commerce.port.in.result.order;

import com.personal.marketnote.commerce.domain.order.Order;
import com.personal.marketnote.commerce.port.out.result.product.ProductInfoResult;
import com.personal.marketnote.common.utility.FormatValidator;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public record OrderHistoryByDateResult(
        LocalDate orderDate,
        Integer count,
        List<GetOrderResult> orders
) {
    public static OrderHistoryByDateResult of(
            LocalDate orderDate,
            List<Order> orders,
            Map<Long, ProductInfoResult> productInfoResultsByPricePolicyId
    ) {
        List<GetOrderResult> orderResults = orders.stream()
                .map(order -> GetOrderResult.from(
                        order,
                        FormatValidator.hasValue(productInfoResultsByPricePolicyId)
                                && FormatValidator.hasValue(productInfoResultsByPricePolicyId.values())
                                ? productInfoResultsByPricePolicyId
                                : Map.of()
                ))
                .toList();

        return new OrderHistoryByDateResult(orderDate, orderResults.size(), orderResults);
    }
}
