package com.personal.marketnote.commerce.port.out.payment.vendor;

import lombok.Builder;

@Builder
public record PaymentCancelVendorResult(
        String resCd,
        String resMsg,
        String amount,
        String rawResponse
) {
    public boolean isSuccess() {
        return "0000".equals(resCd);
    }
}
