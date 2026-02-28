package com.personal.marketnote.commerce.port.in.command.payment;

import lombok.Builder;

@Builder
public record ReadyPaymentCommand(
        Long buyerId,
        String orderKey,
        String payMethod,
        String goodName
) {
}
