package com.personal.marketnote.commerce.port.in.command.quickpayment;

import lombok.Builder;

@Builder
public record DeleteQuickPaymentCardCommand(
        Long quickPaymentCardId,
        Long userId
) {
}
