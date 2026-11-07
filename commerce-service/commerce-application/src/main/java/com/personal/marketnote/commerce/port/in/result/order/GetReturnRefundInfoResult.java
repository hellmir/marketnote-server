package com.personal.marketnote.commerce.port.in.result.order;

import lombok.Builder;

@Builder
public record GetReturnRefundInfoResult(
        long totalProductAmount,
        long returnShippingFee,
        String refundMethod,
        long estimatedRefundAmount,
        long estimatedRefundCash
) {
    public static GetReturnRefundInfoResult of(
            long totalProductAmount,
            long returnShippingFee,
            String refundMethod,
            long estimatedRefundAmount,
            long estimatedRefundCash
    ) {
        return GetReturnRefundInfoResult.builder()
                .totalProductAmount(totalProductAmount)
                .returnShippingFee(returnShippingFee)
                .refundMethod(refundMethod)
                .estimatedRefundAmount(estimatedRefundAmount)
                .estimatedRefundCash(estimatedRefundCash)
                .build();
    }
}
