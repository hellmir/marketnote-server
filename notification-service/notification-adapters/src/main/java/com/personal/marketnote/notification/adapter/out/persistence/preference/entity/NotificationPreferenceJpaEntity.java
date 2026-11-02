package com.personal.marketnote.notification.adapter.out.persistence.preference.entity;

import com.personal.marketnote.common.adapter.out.persistence.audit.BaseGeneralEntity;
import com.personal.marketnote.notification.domain.preference.NotificationPreference;
import com.personal.marketnote.notification.domain.template.NotificationType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification_preference", uniqueConstraints = {
        @UniqueConstraint(name = "uk_notification_preference_user_type", columnNames = {"user_id", "notification_type"})
}, indexes = {
        @Index(name = "idx_notification_preference_user_id", columnList = "user_id")
})
@DynamicInsert
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class NotificationPreferenceJpaEntity extends BaseGeneralEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "notification_type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private NotificationType notificationType;

    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    @Column(name = "consented_at")
    private LocalDateTime consentedAt;

    public static NotificationPreferenceJpaEntity from(NotificationPreference preference) {
        return NotificationPreferenceJpaEntity.builder()
                .userId(preference.getUserId())
                .notificationType(preference.getNotificationType())
                .enabled(preference.isEnabled())
                .consentedAt(preference.getConsentedAt())
                .build();
    }

    public void updateFrom(NotificationPreference preference) {
        if (preference.isInactive()) {
            deactivate();
        }
        this.enabled = preference.isEnabled();
        this.consentedAt = preference.getConsentedAt();
    }
}
