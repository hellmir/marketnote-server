package com.personal.marketnote.notification.domain.preference;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.notification.domain.template.NotificationType;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
public class NotificationPreferenceSnapshotState {
    private Long id;
    private Long userId;
    private NotificationType notificationType;
    private boolean enabled;
    private LocalDateTime consentedAt;
    private EntityStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
}
