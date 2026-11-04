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
    @DisplayName("각 알림 타입의 기본 수신 설정(defaultEnabled)에 따라 초기화한다")
    void shouldInitializePreferencesAccordingToDefaultEnabled() {
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
            assertThat(preference.getConsentedAt()).isNull();
            assertThat(preference.isEnabled())
                    .isEqualTo(preference.getNotificationType().isDefaultEnabled());
        });
        assertThat(saved)
                .extracting(NotificationPreference::getNotificationType)
                .containsExactlyInAnyOrder(NotificationType.values());
    }

    @Test
    @DisplayName("회원 가입 시 MARKETING 수신 설정은 정보통신망법 제50조 옵트인 원칙에 따라 enabled=false로 초기화된다")
    void shouldInitializeMarketingAsDisabled() {
        // given
        InitializeNotificationPreferenceCommand command = new InitializeNotificationPreferenceCommand(100L);
        ArgumentCaptor<List<NotificationPreference>> captor = ArgumentCaptor.forClass(List.class);

        // when
        initializeNotificationPreferenceService.initializeNotificationPreference(command);

        // then
        verify(saveNotificationPreferencePort).saveAll(captor.capture());
        NotificationPreference marketing = captor.getValue().stream()
                .filter(preference -> preference.getNotificationType() == NotificationType.MARKETING)
                .findFirst()
                .orElseThrow(() -> new AssertionError("MARKETING 수신 설정이 초기화되지 않았습니다"));
        assertThat(marketing.isEnabled()).isFalse();
        assertThat(marketing.getConsentedAt()).isNull();
    }

    @Test
    @DisplayName("MARKETING을 제외한 기존 PROMOTIONAL 타입(EVENT/POINT_ACCRUAL 등)은 enabled=true로 초기화된다")
    void shouldInitializeNonMarketingPromotionalTypesAsEnabled() {
        // given
        InitializeNotificationPreferenceCommand command = new InitializeNotificationPreferenceCommand(100L);
        ArgumentCaptor<List<NotificationPreference>> captor = ArgumentCaptor.forClass(List.class);

        // when
        initializeNotificationPreferenceService.initializeNotificationPreference(command);

        // then
        verify(saveNotificationPreferencePort).saveAll(captor.capture());
        List<NotificationPreference> nonMarketing = captor.getValue().stream()
                .filter(preference -> preference.getNotificationType() != NotificationType.MARKETING)
                .toList();
        assertThat(nonMarketing).allSatisfy(preference ->
                assertThat(preference.isEnabled()).isTrue()
        );
    }
}
