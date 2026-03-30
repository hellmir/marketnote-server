package com.personal.marketnote.commerce.port.out.payment.vendor;

import lombok.Builder;

@Builder
public record PaymentCancelVendorCommand(
        String transactionId,
        String cancelType,
        Long cancelAmount,
        Long remainAmount,
        String cancelReason
) {
}
