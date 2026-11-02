package com.personal.marketnote.commerce.adapter.in.web.order.response;

import com.personal.marketnote.commerce.port.in.result.order.GetOrderResult;
import com.personal.marketnote.commerce.port.in.result.order.OrderHistoryByDateResult;

import java.time.LocalDate;
import java.util.List;

public record OrderHistoryByDateResponse(
        LocalDate orderDate,
        Integer count,
        List<GetOrderResult> orders
) {
    public static OrderHistoryByDateResponse from(OrderHistoryByDateResult result) {
        return new OrderHistoryByDateResponse(
                result.orderDate(),
                result.count(),
                result.orders()
        );
    }
}
