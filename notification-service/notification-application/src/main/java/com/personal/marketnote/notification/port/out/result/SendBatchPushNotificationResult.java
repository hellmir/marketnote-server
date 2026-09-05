package com.personal.marketnote.notification.port.out.result;

import java.util.List;

public record SendBatchPushNotificationResult(
        int successCount,
        int failureCount,
        List<FailedToken> failedTokens
) {
    public record FailedToken(
            String deviceToken,
            String errorCode,
            boolean tokenInvalid
    ) {
    }
}
