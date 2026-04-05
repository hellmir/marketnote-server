package com.personal.marketnote.commerce.adapter.in.web.order.response;

import com.personal.marketnote.commerce.domain.order.OrderProduct;

public record InternalOrderProductResponse(
        Long unitAmount
) {
    public static InternalOrderProductResponse from(OrderProduct orderProduct) {
        return new InternalOrderProductResponse(
                orderProduct.getUnitAmount()
        );
    }
}
