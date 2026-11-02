package com.personal.marketnote.commerce.port.in.command.quickpayment;

import lombok.Builder;

@Builder
public record ApproveQuickPaymentCommand(
        Long buyerId,
        String orderKey,
        Long quickPaymentCardId,
        String goodName
) {
}
