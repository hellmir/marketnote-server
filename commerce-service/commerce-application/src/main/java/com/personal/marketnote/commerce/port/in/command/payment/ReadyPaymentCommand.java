package com.personal.marketnote.commerce.port.in.command.payment;

import lombok.Builder;

@Builder
public record ReadyPaymentCommand(
        String orderKey,
        String payMethod,
        String goodName
) {
}
