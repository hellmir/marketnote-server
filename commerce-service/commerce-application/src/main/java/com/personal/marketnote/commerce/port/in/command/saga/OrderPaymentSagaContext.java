package com.personal.marketnote.commerce.port.in.command.saga;

import java.util.List;

public record OrderPaymentSagaContext(
        Long orderId,
        String orderKey,
        Long buyerId,
        Long paymentAmount,
        Long totalAmount,
        Long pointAmount,
        Long totalAccumulatedPoint,
        List<OrderProductItem> orderProducts
) {
    public record OrderProductItem(
            Long pricePolicyId,
            Long sharerId,
            Integer quantity,
            Long unitAmount
    ) {
    }
}
