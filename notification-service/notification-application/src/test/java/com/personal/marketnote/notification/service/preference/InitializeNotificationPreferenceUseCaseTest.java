package com.personal.marketnote.notification.service.preference;

import com.personal.marketnote.notification.domain.preference.NotificationPreference;
import com.personal.marketnote.notification.domain.template.NotificationType;
import com.personal.marketnote.notification.port.in.command.InitializeNotificationPreferenceCommand;
import com.personal.marketnote.notification.port.out.preference.SaveNotificationPreferencePort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class InitializeNotificationPreferenceUseCaseTest {

    @InjectMocks
    private InitializeNotificationPreferenceService initializeNotificationPreferenceService;

    @Mock
    private SaveNotificationPreferencePort saveNotificationPreferencePort;

    @Test
    @DisplayName("모든 알림 타입에 대해 수신 설정을 enabled=true로 초기화한다")
    void shouldInitializeAllNotificationTypesAsEnabled() {
        // given
        InitializeNotificationPreferenceCommand command = new InitializeNotificationPreferenceCommand(100L);
        ArgumentCaptor<List<NotificationPreference>> captor = ArgumentCaptor.forClass(List.class);

        // when
        initializeNotificationPreferenceService.initializeNotificationPreference(command);

        // then
        verify(saveNotificationPreferencePort).saveAll(captor.capture());
        List<NotificationPreference> saved = captor.getValue();

        assertThat(saved).hasSize(NotificationType.values().length);
        assertThat(saved).allSatisfy(preference -> {
            assertThat(preference.getUserId()).isEqualTo(100L);
            assertThat(preference.isEnabled()).isTrue();
            assertThat(preference.getConsentedAt()).isNull();
        });
        assertThat(saved)
                .extracting(NotificationPreference::getNotificationType)
                .containsExactlyInAnyOrder(NotificationType.values());
    }
}
