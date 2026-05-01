package com.personal.marketnote.commerce.port.in.command.payment;

import lombok.Builder;

@Builder
public record RefundPaymentCommand(
        String orderKey,
        Long orderId,
        Long cancelAmount,
        Long paymentAmount,
        boolean isFullCancel,
        Long alreadyRefunded,
        Long returnShippingFee
) {
}
