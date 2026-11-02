package com.personal.marketnote.commerce.port.out.quickpayment;

import lombok.Builder;

@Builder
public record IssueBatchKeyPortCommand(
        String encData,
        String encInfo
) {
}
