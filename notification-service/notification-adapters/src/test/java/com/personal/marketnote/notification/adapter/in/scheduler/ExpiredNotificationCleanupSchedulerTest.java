package com.personal.marketnote.notification.adapter.in.scheduler;

import com.personal.marketnote.notification.port.in.usecase.notification.CleanupExpiredNotificationsUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExpiredNotificationCleanupSchedulerTest {

    @InjectMocks
    private ExpiredNotificationCleanupScheduler expiredNotificationCleanupScheduler;

    @Mock
    private CleanupExpiredNotificationsUseCase cleanupExpiredNotificationsUseCase;

    @Test
    @DisplayName("만료 알림 정리 스케줄러가 UseCase를 호출하여 만료 알림을 비활성화한다")
    void shouldCleanupExpiredNotifications() {
        // given
        when(cleanupExpiredNotificationsUseCase.cleanupExpiredNotifications()).thenReturn(10);

        // when
        expiredNotificationCleanupScheduler.cleanupExpiredNotifications();

        // then
        verify(cleanupExpiredNotificationsUseCase).cleanupExpiredNotifications();
    }

    @Test
    @DisplayName("비활성화 건수가 0이면 UseCase 호출은 정상 수행되고 로그만 다르다")
    void shouldHandleZeroDeletedCount() {
        // given
        when(cleanupExpiredNotificationsUseCase.cleanupExpiredNotifications()).thenReturn(0);

        // when
        expiredNotificationCleanupScheduler.cleanupExpiredNotifications();

        // then
        verify(cleanupExpiredNotificationsUseCase).cleanupExpiredNotifications();
    }
}
