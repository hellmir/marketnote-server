package com.personal.marketnote.notification.service.preference;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.notification.domain.preference.NotificationPreference;
import com.personal.marketnote.notification.domain.preference.NotificationPreferenceSnapshotState;
import com.personal.marketnote.notification.domain.template.NotificationType;
import com.personal.marketnote.notification.port.in.command.SendNotificationCommand;
import com.personal.marketnote.notification.port.in.result.notification.SendNotificationResult;
import com.personal.marketnote.notification.port.in.usecase.notification.SendNotificationUseCase;
import com.personal.marketnote.notification.port.out.preference.FindNotificationPreferencePort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.*;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SendConsentReminderUseCaseTest {

    @InjectMocks
    private SendConsentReminderService sendConsentReminderService;

    @Mock
    private FindNotificationPreferencePort findNotificationPreferencePort;

    @Mock
    private SendNotificationUseCase sendNotificationUseCase;

    @Spy
    private Clock clock = Clock.fixed(
            Instant.parse("2026-09-03T00:00:00Z"),
            ZoneId.of("Asia/Seoul")
    );

    @Test
    @DisplayName("수신 동의 2년 도래 사용자가 없으면 0을 반환한다")
    void shouldReturnZeroWhenNoDueUsers() {
        // given
        when(findNotificationPreferencePort.findConsentReminderDue(any(LocalDateTime.class)))
                .thenReturn(List.of());

        // when
        int result = sendConsentReminderService.sendConsentReminders();

        // then
        assertThat(result).isZero();
        verifyNoInteractions(sendNotificationUseCase);
    }

    @Test
    @DisplayName("수신 동의 2년 도래 사용자에게 재고지 알림을 발송한다")
    void shouldSendConsentReminderToDueUsers() {
        // given
        List<NotificationPreference> duePrefs = List.of(
                createPreference(1L, 100L),
                createPreference(2L, 200L)
        );
        when(findNotificationPreferencePort.findConsentReminderDue(any(LocalDateTime.class)))
                .thenReturn(duePrefs);
        when(sendNotificationUseCase.sendNotification(any(SendNotificationCommand.class)))
                .thenReturn(new SendNotificationResult(1L, "SENT", 1, 0));

        // when
        int result = sendConsentReminderService.sendConsentReminders();

        // then
        assertThat(result).isEqualTo(2);
        verify(sendNotificationUseCase, times(2)).sendNotification(any(SendNotificationCommand.class));
    }

    @Test
    @DisplayName("2년 기준일을 정확하게 계산하여 조회한다")
    void shouldCalculateCorrectThreshold() {
        // given
        when(findNotificationPreferencePort.findConsentReminderDue(any(LocalDateTime.class)))
                .thenReturn(List.of());

        // when
        sendConsentReminderService.sendConsentReminders();

        // then
        ArgumentCaptor<LocalDateTime> captor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(findNotificationPreferencePort).findConsentReminderDue(captor.capture());

        LocalDateTime threshold = captor.getValue();
        LocalDateTime expected = LocalDateTime.now(clock).minusYears(2);
        assertThat(threshold).isEqualTo(expected);
    }

    @Test
    @DisplayName("CONSENT_REMINDER 템플릿 코드와 PUSH_AND_IN_APP 채널로 발송한다")
    void shouldUseCorrectTemplateAndChannel() {
        // given
        List<NotificationPreference> duePrefs = List.of(createPreference(1L, 100L));
        when(findNotificationPreferencePort.findConsentReminderDue(any(LocalDateTime.class)))
                .thenReturn(duePrefs);
        when(sendNotificationUseCase.sendNotification(any(SendNotificationCommand.class)))
                .thenReturn(new SendNotificationResult(1L, "SENT", 1, 0));

        // when
        sendConsentReminderService.sendConsentReminders();

        // then
        ArgumentCaptor<SendNotificationCommand> captor = ArgumentCaptor.forClass(SendNotificationCommand.class);
        verify(sendNotificationUseCase).sendNotification(captor.capture());

        SendNotificationCommand command = captor.getValue();
        assertThat(command.templateCode()).isEqualTo("CONSENT_REMINDER");
        assertThat(command.deliveryChannel()).isEqualTo("PUSH_AND_IN_APP");
        assertThat(command.userId()).isEqualTo(100L);
    }

    @Test
    @DisplayName("개별 발송 실패 시 나머지 사용자에게 계속 발송한다")
    void shouldContinueOnIndividualFailure() {
        // given
        List<NotificationPreference> duePrefs = List.of(
                createPreference(1L, 100L),
                createPreference(2L, 200L)
        );
        when(findNotificationPreferencePort.findConsentReminderDue(any(LocalDateTime.class)))
                .thenReturn(duePrefs);
        when(sendNotificationUseCase.sendNotification(any(SendNotificationCommand.class)))
                .thenThrow(new RuntimeException("template not found"))
                .thenReturn(new SendNotificationResult(2L, "SENT", 1, 0));

        // when
        int result = sendConsentReminderService.sendConsentReminders();

        // then
        assertThat(result).isEqualTo(1);
        verify(sendNotificationUseCase, times(2)).sendNotification(any(SendNotificationCommand.class));
    }

    private NotificationPreference createPreference(Long id, Long userId) {
        return NotificationPreference.from(
                NotificationPreferenceSnapshotState.builder()
                        .id(id)
                        .userId(userId)
                        .notificationType(NotificationType.EVENT)
                        .enabled(true)
                        .consentedAt(LocalDateTime.of(2024, 4, 10, 9, 0))
                        .status(EntityStatus.ACTIVE)
                        .createdAt(LocalDateTime.now())
                        .modifiedAt(LocalDateTime.now())
                        .build()
        );
    }
}
