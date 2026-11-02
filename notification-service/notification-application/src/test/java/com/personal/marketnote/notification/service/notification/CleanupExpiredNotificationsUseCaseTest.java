package com.personal.marketnote.notification.service.notification;

import com.personal.marketnote.notification.port.out.notification.DeleteNotificationPort;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CleanupExpiredNotificationsUseCaseTest {

    @InjectMocks
    private CleanupExpiredNotificationsService cleanupExpiredNotificationsService;

    @Mock
    private DeleteNotificationPort deleteNotificationPort;

    @Spy
    private Clock clock = Clock.fixed(
            Instant.parse("2026-09-03T05:00:00Z"),
            ZoneId.of("Asia/Seoul")
    );

    @Test
    @DisplayName("90일 이전 기준일을 계산하여 deactivateExpiredNotifications를 호출한다")
    void shouldCallDeactivateWithCorrectThreshold() {
        // given
        when(deleteNotificationPort.deactivateExpiredNotifications(any(LocalDateTime.class)))
                .thenReturn(10);

        // when
        int result = cleanupExpiredNotificationsService.cleanupExpiredNotifications();

        // then
        ArgumentCaptor<LocalDateTime> captor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(deleteNotificationPort).deactivateExpiredNotifications(captor.capture());

        LocalDateTime threshold = captor.getValue();
        LocalDateTime expected = LocalDateTime.now(clock).minusDays(90);
        assertThat(threshold).isEqualTo(expected);
        assertThat(result).isEqualTo(10);
    }

    @Test
    @DisplayName("삭제된 알림이 없으면 0을 반환한다")
    void shouldReturnZeroWhenNoExpiredNotifications() {
        // given
        when(deleteNotificationPort.deactivateExpiredNotifications(any(LocalDateTime.class)))
                .thenReturn(0);

        // when
        int result = cleanupExpiredNotificationsService.cleanupExpiredNotifications();

        // then
        assertThat(result).isZero();
    }
}
