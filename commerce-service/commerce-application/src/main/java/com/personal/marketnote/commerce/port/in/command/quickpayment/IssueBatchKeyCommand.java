package com.personal.marketnote.commerce.port.in.command.quickpayment;

import lombok.Builder;

@Builder
public record IssueBatchKeyCommand(
        Long userId,
        String encData,
        String encInfo
) {
}
