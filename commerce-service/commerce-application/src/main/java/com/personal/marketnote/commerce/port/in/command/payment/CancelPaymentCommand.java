package com.personal.marketnote.commerce.port.in.command.payment;

import lombok.Builder;

@Builder
public record CancelPaymentCommand(
        Long buyerId,
        String orderKey,
        CancelType cancelType,
        Long cancelAmount,
        String cancelReason
) {
    public enum CancelType {
        FULL, PARTIAL
    }

    public boolean isFullCancel() {
        return cancelType == CancelType.FULL;
    }
}
