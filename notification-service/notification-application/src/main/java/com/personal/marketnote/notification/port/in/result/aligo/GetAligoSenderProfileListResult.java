package com.personal.marketnote.notification.port.in.result.aligo;

import java.util.List;

public record GetAligoSenderProfileListResult(
        List<AligoSenderProfileResult> profiles
) {

    public record AligoSenderProfileResult(
            Long id,
            String senderKey,
            String plusId,
            String name,
            String categoryCode,
            String profileInspectionStatus,
            String senderProfileStatus,
            boolean alimUseYn
    ) {
    }
}
