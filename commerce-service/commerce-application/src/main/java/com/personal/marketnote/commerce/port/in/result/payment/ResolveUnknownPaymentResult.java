package com.personal.marketnote.commerce.port.in.result.payment;

import com.personal.marketnote.commerce.domain.payment.Payment;
import lombok.Builder;

@Builder
public record ResolveUnknownPaymentResult(
        String orderKey,
        String resolvedStatus,
        Long orderId
) {
    public static ResolveUnknownPaymentResult of(String orderKey, String resolvedStatus, Payment payment) {
        return new ResolveUnknownPaymentResult(orderKey, resolvedStatus, payment.getOrderId());
    }
}
