package com.personal.marketnote.notification.service.preference;

import com.personal.marketnote.notification.domain.preference.NotificationPreference;
import com.personal.marketnote.notification.domain.preference.NotificationPreferenceSnapshotState;
import com.personal.marketnote.notification.domain.template.NotificationType;
import com.personal.marketnote.notification.port.in.result.preference.GetNotificationPreferenceResult;
import com.personal.marketnote.notification.port.out.preference.FindNotificationPreferencePort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static com.personal.marketnote.common.domain.EntityStatus.ACTIVE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetNotificationPreferenceUseCaseTest {

    @InjectMocks
    private GetNotificationPreferenceService getNotificationPreferenceService;

    @Mock
    private FindNotificationPreferencePort findNotificationPreferencePort;

    @Test
    @DisplayName("사용자의 알림 수신 설정 목록을 반환한다")
    void shouldReturnNotificationPreferencesForUser() {
        // given
        Long userId = 100L;
        LocalDateTime consentedAt = LocalDateTime.of(2026, 4, 9, 10, 0, 0);

        NotificationPreference preference1 = NotificationPreference.from(
                NotificationPreferenceSnapshotState.builder()
                        .id(1L)
                        .userId(userId)
                        .notificationType(NotificationType.ORDER_PAYMENT_COMPLETED)
                        .enabled(true)
                        .consentedAt(consentedAt)
                        .status(ACTIVE)
                        .createdAt(consentedAt)
                        .modifiedAt(consentedAt)
                        .build()
        );

        NotificationPreference preference2 = NotificationPreference.from(
                NotificationPreferenceSnapshotState.builder()
                        .id(2L)
                        .userId(userId)
                        .notificationType(NotificationType.SHIPPING_STARTED)
                        .enabled(false)
                        .consentedAt(null)
                        .status(ACTIVE)
                        .createdAt(consentedAt)
                        .modifiedAt(consentedAt)
                        .build()
        );

        when(findNotificationPreferencePort.findAllByUserId(userId))
                .thenReturn(List.of(preference1, preference2));

        // when
        List<GetNotificationPreferenceResult> results = getNotificationPreferenceService.getNotificationPreferences(userId);

        // then
        assertThat(results).hasSize(2);

        GetNotificationPreferenceResult result1 = results.get(0);
        assertThat(result1.notificationType()).isEqualTo("ORDER_PAYMENT_COMPLETED");
        assertThat(result1.description()).isEqualTo("주문 결제 완료");
        assertThat(result1.enabled()).isTrue();
        assertThat(result1.consentedAt()).isEqualTo(consentedAt);

        GetNotificationPreferenceResult result2 = results.get(1);
        assertThat(result2.notificationType()).isEqualTo("SHIPPING_STARTED");
        assertThat(result2.description()).isEqualTo("배송 시작");
        assertThat(result2.enabled()).isFalse();
        assertThat(result2.consentedAt()).isNull();

        verify(findNotificationPreferencePort).findAllByUserId(userId);
    }

    @Test
    @DisplayName("알림 수신 설정이 없으면 빈 목록을 반환한다")
    void shouldReturnEmptyListWhenNoPreferencesExist() {
        // given
        Long userId = 200L;

        when(findNotificationPreferencePort.findAllByUserId(userId))
                .thenReturn(List.of());

        // when
        List<GetNotificationPreferenceResult> results = getNotificationPreferenceService.getNotificationPreferences(userId);

        // then
        assertThat(results).isEmpty();

        verify(findNotificationPreferencePort).findAllByUserId(userId);
    }
}
