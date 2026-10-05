package com.personal.marketnote.notification.adapter.in.scheduler;

import com.personal.marketnote.notification.port.in.usecase.notification.PublishScheduledNotificationsUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScheduledNotificationPublisherTest {

    @InjectMocks
    private ScheduledNotificationPublisher scheduledNotificationPublisher;

    @Mock
    private PublishScheduledNotificationsUseCase publishScheduledNotificationsUseCase;

    @Test
    @DisplayName("예약 알림 발송 스케줄러가 UseCase를 호출하여 예약 알림을 발송한다")
    void shouldPublishScheduledNotifications() {
        // given
        when(publishScheduledNotificationsUseCase.publishScheduledNotifications()).thenReturn(5);

        // when
        scheduledNotificationPublisher.publishScheduledNotifications();

        // then
        verify(publishScheduledNotificationsUseCase).publishScheduledNotifications();
    }

    @Test
    @DisplayName("처리 건수가 0이면 UseCase 호출은 정상 수행되고 로그만 다르다")
    void shouldHandleZeroProcessedCount() {
        // given
        when(publishScheduledNotificationsUseCase.publishScheduledNotifications()).thenReturn(0);

        // when
        scheduledNotificationPublisher.publishScheduledNotifications();

        // then
        verify(publishScheduledNotificationsUseCase).publishScheduledNotifications();
    }
}
