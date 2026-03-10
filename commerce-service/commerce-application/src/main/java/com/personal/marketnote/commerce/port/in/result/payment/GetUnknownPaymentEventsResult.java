package com.personal.marketnote.commerce.port.in.result.payment;

import com.personal.marketnote.commerce.domain.payment.PspPaymentEvent;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record GetUnknownPaymentEventsResult(
        Long id,
        Long orderId,
        String orderKey,
        Long amount,
        String method,
        String resultCode,
        String resultMessage,
        LocalDateTime createdAt
) {
    public static GetUnknownPaymentEventsResult from(PspPaymentEvent event) {
        return GetUnknownPaymentEventsResult.builder()
                .id(event.getId())
                .orderId(event.getOrderId())
                .orderKey(event.getOrderKey())
                .amount(event.getAmount())
                .method(event.getMethod())
                .resultCode(event.getResultCode())
                .resultMessage(event.getResultMessage())
                .createdAt(event.getCreatedAt())
                .build();
    }
}
