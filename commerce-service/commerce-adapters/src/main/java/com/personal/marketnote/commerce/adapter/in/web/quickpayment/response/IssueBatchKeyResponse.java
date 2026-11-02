package com.personal.marketnote.commerce.adapter.in.web.quickpayment.response;

import com.personal.marketnote.commerce.port.in.result.quickpayment.IssueBatchKeyResult;
import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
public record IssueBatchKeyResponse(
        Long quickPaymentCardId,
        String cardCode,
        String cardName,
        String maskedCardNumber,
        String cardBinType01,
        String cardBinType02
) {
    public static IssueBatchKeyResponse from(IssueBatchKeyResult result) {
        return IssueBatchKeyResponse.builder()
                .quickPaymentCardId(result.quickPaymentCardId())
                .cardCode(result.cardCode())
                .cardName(result.cardName())
                .maskedCardNumber(result.maskedCardNumber())
                .cardBinType01(result.cardBinType01())
                .cardBinType02(result.cardBinType02())
                .build();
    }
}
