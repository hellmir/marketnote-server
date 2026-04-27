package com.personal.marketnote.commerce.port.in.command.saga;

import com.personal.marketnote.commerce.port.in.command.saga.OrderPaymentSagaContext.OrderProductItem;

import java.util.List;

public record OrderCancelSagaContext(
        Long orderId,
        String orderKey,
        Long buyerId,
        Long cancelAmount,
        Long paymentAmount,
        Long pointAmount,
        Long shippingFee,
        boolean isFullCancel,
        Long alreadyRefunded,
        String originalStatus,
        String reasonCategory,
        String reason,
        List<OrderProductItem> orderProducts
) {
}
