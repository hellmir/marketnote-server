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
}
