package com.personal.marketnote.commerce.adapter.in.web.payment.response;

import com.personal.marketnote.commerce.port.in.result.payment.GetUnknownPaymentEventsResult;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record GetUnknownPaymentEventsResponse(
        Long id,
        Long orderId,
        String orderKey,
        Long amount,
        String method,
        String resultCode,
        String resultMessage,
        LocalDateTime createdAt
) {
    public static GetUnknownPaymentEventsResponse from(GetUnknownPaymentEventsResult result) {
        return GetUnknownPaymentEventsResponse.builder()
                .id(result.id())
                .orderId(result.orderId())
                .orderKey(result.orderKey())
                .amount(result.amount())
                .method(result.method())
                .resultCode(result.resultCode())
                .resultMessage(result.resultMessage())
                .createdAt(result.createdAt())
                .build();
    }
}
