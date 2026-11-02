package com.personal.marketnote.commerce.port.in.result.order;

import com.personal.marketnote.commerce.domain.order.Order;
import com.personal.marketnote.commerce.port.out.result.product.ProductInfoResult;
import com.personal.marketnote.common.utility.FormatValidator;
import lombok.AccessLevel;
import lombok.Builder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Builder(access = AccessLevel.PRIVATE)
public record GetBuyerOrderHistoryResult(
        List<OrderHistoryByDateResult> orderHistory
) {
    public static GetBuyerOrderHistoryResult from(
            List<Order> orders,
            Map<Long, ProductInfoResult> productInfoResultsByPricePolicyId
    ) {
        Map<LocalDate, List<Order>> ordersByOrderDate = new LinkedHashMap<>();

        for (Order order : orders) {
            LocalDate orderDate = FormatValidator.hasValue(order.getCreatedAt())
                    ? order.getCreatedAt().toLocalDate()
                    : LocalDate.now();

            ordersByOrderDate.computeIfAbsent(orderDate, key -> new ArrayList<>())
                    .add(order);
        }

        List<OrderHistoryByDateResult> histories = ordersByOrderDate.entrySet().stream()
                .map(entry -> OrderHistoryByDateResult.of(
                        entry.getKey(),
                        entry.getValue(),
                        FormatValidator.hasValue(productInfoResultsByPricePolicyId)
                                && FormatValidator.hasValue(productInfoResultsByPricePolicyId.values())
                                ? productInfoResultsByPricePolicyId
                                : Map.of()
                ))
                .toList();

        return new GetBuyerOrderHistoryResult(histories);
    }
}
