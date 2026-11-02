package com.personal.marketnote.notification.domain.preference;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.notification.domain.template.NotificationType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NotificationPreferenceTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 4, 9, 10, 0);

    @Test
    @DisplayName("CreateState로 알림 수신 설정을 생성한다")
    void shouldCreateFromCreateState() {
        NotificationPreference preference = NotificationPreference.from(NotificationPreferenceCreateState.builder()
                .userId(100L)
                .notificationType(NotificationType.ORDER_PAYMENT_COMPLETED)
                .enabled(true)
                .build());

        assertThat(preference.getUserId()).isEqualTo(100L);
        assertThat(preference.getNotificationType()).isEqualTo(NotificationType.ORDER_PAYMENT_COMPLETED);
        assertThat(preference.isEnabled()).isTrue();
        assertThat(preference.getConsentedAt()).isNull();
        assertThat(preference.isActive()).isTrue();
    }

    @Test
    @DisplayName("userId가 null이면 예외를 던진다")
    void shouldThrowExceptionWhenUserIdIsNull() {
        NotificationPreferenceCreateState state = NotificationPreferenceCreateState.builder()
                .userId(null)
                .notificationType(NotificationType.ORDER_PAYMENT_COMPLETED)
                .enabled(true)
                .build();

        assertThatThrownBy(() -> NotificationPreference.from(state))
                .isInstanceOf(InvalidNotificationPreferenceException.class);
    }

    @Test
    @DisplayName("notificationType이 null이면 예외를 던진다")
    void shouldThrowExceptionWhenNotificationTypeIsNull() {
        NotificationPreferenceCreateState state = NotificationPreferenceCreateState.builder()
                .userId(100L)
                .notificationType(null)
                .enabled(true)
                .build();

        assertThatThrownBy(() -> NotificationPreference.from(state))
                .isInstanceOf(InvalidNotificationPreferenceException.class);
    }

    @Test
    @DisplayName("SnapshotState로 알림 수신 설정을 복원한다")
    void shouldRestoreFromSnapshotState() {
        NotificationPreference preference = NotificationPreference.from(NotificationPreferenceSnapshotState.builder()
                .id(1L)
                .userId(100L)
                .notificationType(NotificationType.ORDER_PAYMENT_COMPLETED)
                .enabled(false)
                .consentedAt(NOW)
                .status(EntityStatus.ACTIVE)
                .createdAt(NOW)
                .modifiedAt(NOW)
                .build());

        assertThat(preference.getId()).isEqualTo(1L);
        assertThat(preference.isEnabled()).isFalse();
        assertThat(preference.getConsentedAt()).isEqualTo(NOW);
    }

    @Test
    @DisplayName("enable/disable 상태 전이로 활성화 여부를 변경한다")
    void shouldToggleEnabled() {
        NotificationPreference preference = NotificationPreference.from(NotificationPreferenceCreateState.builder()
                .userId(100L)
                .notificationType(NotificationType.EVENT)
                .enabled(true)
                .build());

        preference.disable(NOW);

        assertThat(preference.isEnabled()).isFalse();
        assertThat(preference.getConsentedAt()).isNull();

        preference.enable(NOW);

        assertThat(preference.isEnabled()).isTrue();
        assertThat(preference.getConsentedAt()).isEqualTo(NOW);
    }
}
