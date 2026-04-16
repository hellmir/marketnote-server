package com.personal.marketnote.commerce.port.out.quickpayment;

import lombok.Builder;

@Builder
public record IssueBatchKeyPortResult(
        boolean success,
        String resultCode,
        String resultMessage,
        String batchKey,
        String cardCode,
        String cardName,
        String cardBinType01,
        String cardBinType02,
        String rawResponse
) {
}
