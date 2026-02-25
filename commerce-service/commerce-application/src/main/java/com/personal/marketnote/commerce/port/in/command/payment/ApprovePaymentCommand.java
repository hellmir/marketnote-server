package com.personal.marketnote.commerce.port.in.command.payment;

import lombok.Builder;

@Builder
public record ApprovePaymentCommand(
        Long buyerId,
        String orderKey,
        String encData,
        String encInfo,
        String payType
) {
}
