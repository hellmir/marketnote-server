package com.personal.marketnote.notification.domain.preference;

import com.personal.marketnote.notification.domain.template.NotificationType;
import lombok.*;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
public class NotificationPreferenceCreateState {
    private Long userId;
    private NotificationType notificationType;
    private boolean enabled;
}
