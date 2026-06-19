package com.personal.marketnote.notification.service.preference;

import com.personal.marketnote.notification.domain.preference.NotificationPreference;
import com.personal.marketnote.notification.domain.preference.NotificationPreferenceNotFoundException;
import com.personal.marketnote.notification.domain.preference.NotificationPreferenceSnapshotState;
import com.personal.marketnote.notification.domain.template.NotificationType;
import com.personal.marketnote.notification.port.in.command.UpdateNotificationPreferenceCommand;
import com.personal.marketnote.notification.port.out.preference.FindNotificationPreferencePort;
import com.personal.marketnote.notification.port.out.preference.UpdateNotificationPreferencePort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static com.personal.marketnote.common.domain.EntityStatus.ACTIVE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateNotificationPreferenceUseCaseTest {

    @InjectMocks
    private UpdateNotificationPreferenceService updateNotificationPreferenceService;

    @Mock
    private FindNotificationPreferencePort findNotificationPreferencePort;

    @Mock
    private UpdateNotificationPreferencePort updateNotificationPreferencePort;

    @Spy
    private Clock clock = Clock.fixed(Instant.parse("2026-06-03T10:00:00Z"), ZoneId.of("Asia/Seoul"));

    @Test
    @DisplayName("알림 수신 설정을 활성화하면 enabled=true, consentedAt이 설정된다")
    void shouldEnableNotificationPreference() {
        // given
        Long userId = 100L;
        NotificationPreference preference = NotificationPreference.from(
                NotificationPreferenceSnapshotState.builder()
                        .id(1L)
                        .userId(userId)
                        .notificationType(NotificationType.EVENT)
                        .enabled(false)
                        .consentedAt(null)
                        .status(ACTIVE)
                        .createdAt(LocalDateTime.of(2026, 4, 1, 0, 0))
                        .modifiedAt(LocalDateTime.of(2026, 4, 1, 0, 0))
                        .build()
        );

        when(findNotificationPreferencePort.findByUserIdAndNotificationType(userId, NotificationType.EVENT))
                .thenReturn(Optional.of(preference));

        UpdateNotificationPreferenceCommand command = new UpdateNotificationPreferenceCommand(userId, "EVENT", true);

        // when
        updateNotificationPreferenceService.updateNotificationPreference(command);

        // then
        ArgumentCaptor<NotificationPreference> captor = ArgumentCaptor.forClass(NotificationPreference.class);
        verify(updateNotificationPreferencePort).update(captor.capture());

        NotificationPreference updated = captor.getValue();
        assertThat(updated.isEnabled()).isTrue();
        assertThat(updated.getConsentedAt()).isEqualTo(LocalDateTime.of(2026, 4, 9, 19, 0, 0));
    }

    @Test
    @DisplayName("알림 수신 설정을 비활성화하면 enabled=false가 된다")
    void shouldDisableNotificationPreference() {
        // given
        Long userId = 100L;
        LocalDateTime consentedAt = LocalDateTime.of(2026, 4, 1, 0, 0);
        NotificationPreference preference = NotificationPreference.from(
                NotificationPreferenceSnapshotState.builder()
                        .id(1L)
                        .userId(userId)
                        .notificationType(NotificationType.EVENT)
                        .enabled(true)
                        .consentedAt(consentedAt)
                        .status(ACTIVE)
                        .createdAt(consentedAt)
                        .modifiedAt(consentedAt)
                        .build()
        );

        when(findNotificationPreferencePort.findByUserIdAndNotificationType(userId, NotificationType.EVENT))
                .thenReturn(Optional.of(preference));

        UpdateNotificationPreferenceCommand command = new UpdateNotificationPreferenceCommand(userId, "EVENT", false);

        // when
        updateNotificationPreferenceService.updateNotificationPreference(command);

        // then
        ArgumentCaptor<NotificationPreference> captor = ArgumentCaptor.forClass(NotificationPreference.class);
        verify(updateNotificationPreferencePort).update(captor.capture());

        NotificationPreference updated = captor.getValue();
        assertThat(updated.isEnabled()).isFalse();
    }

    @Test
    @DisplayName("존재하지 않는 알림 수신 설정을 변경하면 예외가 발생한다")
    void shouldThrowExceptionWhenPreferenceNotFound() {
        // given
        Long userId = 200L;

        when(findNotificationPreferencePort.findByUserIdAndNotificationType(userId, NotificationType.ORDER_PAYMENT_COMPLETED))
                .thenReturn(Optional.empty());

        UpdateNotificationPreferenceCommand command = new UpdateNotificationPreferenceCommand(userId, "ORDER_PAYMENT_COMPLETED", true);

        // when & then
        assertThatThrownBy(() -> updateNotificationPreferenceService.updateNotificationPreference(command))
                .isInstanceOf(NotificationPreferenceNotFoundException.class);
    }
}
