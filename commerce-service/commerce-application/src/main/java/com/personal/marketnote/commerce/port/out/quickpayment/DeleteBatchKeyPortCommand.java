package com.personal.marketnote.commerce.port.out.quickpayment;

import lombok.Builder;

@Builder
public record DeleteBatchKeyPortCommand(
        String batchKey,
        String groupId
) {
}
