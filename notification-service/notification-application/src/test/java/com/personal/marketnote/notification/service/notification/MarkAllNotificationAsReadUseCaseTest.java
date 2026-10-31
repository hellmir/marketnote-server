package com.personal.marketnote.notification.service.notification;

import com.personal.marketnote.notification.port.out.notification.UpdateNotificationPort;
import com.personal.marketnote.notification.port.out.sse.NotificationSseEventType;
import com.personal.marketnote.notification.port.out.sse.PublishSseEventPort;
import com.personal.marketnote.notification.port.out.sse.payload.UnreadCountChangedPayload;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MarkAllNotificationAsReadUseCaseTest {

    @InjectMocks
    private MarkAllNotificationAsReadService markAllNotificationAsReadService;

    @Mock
    private UpdateNotificationPort updateNotificationPort;

    @Mock
    private PublishSseEventPort publishSseEventPort;

    @Test
    @DisplayName("미읽음 알림이 존재하면 전체 읽음 처리 후 SSE 이벤트를 발행한다")
    void shouldMarkAllAsReadAndPublishSseWhenUnreadExists() {
        // given
        Long userId = 100L;
        when(updateNotificationPort.markAllAsReadByUserId(userId)).thenReturn(5);

        // when
        markAllNotificationAsReadService.markAllAsRead(userId);

        // then
        verify(updateNotificationPort).markAllAsReadByUserId(userId);
        verify(publishSseEventPort).publish(
                userId, NotificationSseEventType.UNREAD_COUNT_CHANGED, UnreadCountChangedPayload.of(0L)
        );
    }

    @Test
    @DisplayName("미읽음 알림이 0건이면 SSE 이벤트를 발행하지 않는다 (멱등)")
    void shouldNotPublishSseWhenNoUnread() {
        // given
        Long userId = 100L;
        when(updateNotificationPort.markAllAsReadByUserId(userId)).thenReturn(0);

        // when
        markAllNotificationAsReadService.markAllAsRead(userId);

        // then
        verify(updateNotificationPort).markAllAsReadByUserId(userId);
        verifyNoInteractions(publishSseEventPort);
    }
}
