package com.personal.marketnote.commerce.port.in.result.quickpayment;

import lombok.Builder;

@Builder
public record IssueBatchKeyResult(
        Long quickPaymentCardId,
        String cardCode,
        String cardName,
        String maskedCardNumber,
        String cardBinType01,
        String cardBinType02
) {
}
