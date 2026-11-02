package com.personal.marketnote.commerce.port.in.result.order;

import com.personal.marketnote.commerce.domain.order.Order;

public record GetOrderKeyResult(
        String orderKey
) {
    public static GetOrderKeyResult from(Order order) {
        return new GetOrderKeyResult(order.getOrderKey().toString());
    }
}
