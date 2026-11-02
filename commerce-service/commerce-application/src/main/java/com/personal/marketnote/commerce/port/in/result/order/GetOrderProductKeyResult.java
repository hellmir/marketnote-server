package com.personal.marketnote.commerce.port.in.result.order;

import com.personal.marketnote.commerce.domain.order.OrderProduct;

public record GetOrderProductKeyResult(
        String orderProductKey
) {
    public static GetOrderProductKeyResult from(OrderProduct orderProduct) {
        return new GetOrderProductKeyResult(orderProduct.getOrderProductKey().toString());
    }
}
