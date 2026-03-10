package com.personal.marketnote.commerce.adapter.in.web.payment.response;

import com.personal.marketnote.commerce.port.in.result.payment.ResolveUnknownPaymentResult;
import lombok.Builder;

@Builder
public record ResolveUnknownPaymentResponse(
        String orderKey,
        String resolvedStatus,
        Long orderId
) {
    public static ResolveUnknownPaymentResponse from(ResolveUnknownPaymentResult result) {
        return ResolveUnknownPaymentResponse.builder()
                .orderKey(result.orderKey())
                .resolvedStatus(result.resolvedStatus())
                .orderId(result.orderId())
                .build();
    }
}
