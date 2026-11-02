package com.personal.marketnote.commerce.port.out.quickpayment;

import lombok.Builder;

@Builder
public record DeleteBatchKeyPortResult(
        boolean success,
        String resultCode,
        String resultMessage
) {
}
