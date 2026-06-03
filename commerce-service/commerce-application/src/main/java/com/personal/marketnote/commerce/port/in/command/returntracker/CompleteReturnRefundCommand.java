package com.personal.marketnote.commerce.port.in.command.returntracker;

import lombok.Builder;

@Builder
public record CompleteReturnRefundCommand(
        Long orderId,
        String orderKey,
        Long buyerId,
        Long returnAmount,
        Long paymentAmount,
        Long pointAmount,
        Long shippingFee,
        boolean isFullReturn,
        Long returnShippingFee
) {
}
