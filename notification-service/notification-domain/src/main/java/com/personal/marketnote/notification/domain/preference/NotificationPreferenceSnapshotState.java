package com.personal.marketnote.notification.domain.preference;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.notification.domain.template.NotificationType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class NotificationPreferenceSnapshotState {
    private final Long id;
    private final Long userId;
    private final NotificationType notificationType;
    private final boolean enabled;
    private final LocalDateTime consentedAt;
    private final EntityStatus status;
    private final LocalDateTime createdAt;
    private final LocalDateTime modifiedAt;
}
