package com.personal.marketnote.notification.domain.preference;

import com.personal.marketnote.common.domain.BaseDomain;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.notification.domain.template.NotificationType;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class NotificationPreference extends BaseDomain {
    private Long id;
    private Long userId;
    private NotificationType notificationType;
    private boolean enabled;
    private LocalDateTime consentedAt;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    public static NotificationPreference from(NotificationPreferenceCreateState state) {
        validate(state.getUserId(), state.getNotificationType());

        NotificationPreference preference = NotificationPreference.builder()
                .userId(state.getUserId())
                .notificationType(state.getNotificationType())
                .enabled(state.isEnabled())
                .build();
        preference.activate();
        return preference;
    }

    public static NotificationPreference from(NotificationPreferenceSnapshotState state) {
        NotificationPreference preference = NotificationPreference.builder()
                .id(state.getId())
                .userId(state.getUserId())
                .notificationType(state.getNotificationType())
                .enabled(state.isEnabled())
                .consentedAt(state.getConsentedAt())
                .createdAt(state.getCreatedAt())
                .modifiedAt(state.getModifiedAt())
                .build();
        preference.status = state.getStatus();
        return preference;
    }

    public void enable(LocalDateTime now) {
        this.enabled = true;
        this.consentedAt = now;
    }

    public void disable(LocalDateTime now) {
        this.enabled = false;
    }

    private static void validate(Long userId, NotificationType notificationType) {
        if (FormatValidator.hasNoValue(userId)) {
            throw new InvalidNotificationPreferenceException("사용자 ID는 필수입니다.");
        }
        if (FormatValidator.hasNoValue(notificationType)) {
            throw new InvalidNotificationPreferenceException("알림 타입은 필수입니다.");
        }
    }
}
