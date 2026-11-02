package com.personal.marketnote.commerce.port.in.result.payment;

import lombok.Builder;

@Builder
public record ResolveUnknownPaymentResult(
        String orderKey,
        String resolvedStatus,
        Long orderId
) {
}
