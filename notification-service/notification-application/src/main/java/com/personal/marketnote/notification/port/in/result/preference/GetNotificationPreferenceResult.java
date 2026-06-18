package com.personal.marketnote.notification.port.in.result.preference;

import java.time.LocalDateTime;

public record GetNotificationPreferenceResult(
        String notificationType,
        String description,
        boolean enabled,
        LocalDateTime consentedAt
) {
}
