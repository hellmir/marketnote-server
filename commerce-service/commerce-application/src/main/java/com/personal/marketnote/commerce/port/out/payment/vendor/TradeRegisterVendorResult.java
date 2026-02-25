package com.personal.marketnote.commerce.port.out.payment.vendor;

import lombok.Builder;

@Builder
public record TradeRegisterVendorResult(
        String resCd,
        String resMsg,
        String approvalKey,
        String payUrl,
        String traceNo,
        String rawResponse
) {
    public boolean isSuccess() {
        return "0000".equals(resCd);
    }
}
