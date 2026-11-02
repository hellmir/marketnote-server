package com.personal.marketnote.commerce.adapter.in.web.order.response;

import com.personal.marketnote.commerce.port.in.result.order.GetReturnRefundInfoResult;
import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
public record GetReturnRefundInfoResponse(
        long totalProductAmount,
        long returnShippingFee,
        String refundMethod,
        long estimatedRefundAmount,
        long estimatedRefundCash
) {
    public static GetReturnRefundInfoResponse from(GetReturnRefundInfoResult result) {
        return GetReturnRefundInfoResponse.builder()
                .totalProductAmount(result.totalProductAmount())
                .returnShippingFee(result.returnShippingFee())
                .refundMethod(result.refundMethod())
                .estimatedRefundAmount(result.estimatedRefundAmount())
                .estimatedRefundCash(result.estimatedRefundCash())
                .build();
    }
}
