package com.personal.marketnote.commerce.port.out.payment.vendor;

import lombok.Builder;

@Builder
public record TradeRegisterVendorResult(
        boolean success,
        String resultCode,
        String resultMessage,
        String approvalKey,
        String payUrl,
        String traceNo,
        String rawResponse
) {
}
