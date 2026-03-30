package com.personal.marketnote.commerce.port.out.payment.vendor;

import lombok.Builder;

@Builder
public record PaymentCancelVendorResult(
        boolean success,
        String resultCode,
        String resultMessage,
        String amount,
        String rawResponse
) {
}
