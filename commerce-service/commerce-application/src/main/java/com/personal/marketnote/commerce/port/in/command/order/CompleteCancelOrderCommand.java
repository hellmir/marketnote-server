package com.personal.marketnote.commerce.port.in.command.order;

import lombok.Builder;

@Builder
public record CompleteCancelOrderCommand(
        Long orderId,
        String orderKey,
        Long buyerId,
        Long cancelAmount,
        Long paymentAmount,
        Long pointAmount,
        Long shippingFee,
        boolean isFullCancel,
        Long alreadyRefunded,
        String reasonCategory,
        String reason
) {
}
