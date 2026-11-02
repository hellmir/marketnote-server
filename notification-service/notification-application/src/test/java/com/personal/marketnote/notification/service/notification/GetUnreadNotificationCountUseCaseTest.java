package com.personal.marketnote.notification.service.notification;

import com.personal.marketnote.notification.port.in.result.notification.GetUnreadNotificationCountResult;
import com.personal.marketnote.notification.port.out.notification.FindNotificationPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetUnreadNotificationCountUseCaseTest {

    @InjectMocks
    private GetUnreadNotificationCountService getUnreadNotificationCountService;

    @Mock
    private FindNotificationPort findNotificationPort;

    @Test
    @DisplayName("읽지 않은 알림이 5건이면 unreadCount=5를 반환한다")
    void shouldReturnUnreadCount() {
        // given
        Long userId = 1L;
        when(findNotificationPort.countUnreadByUserId(userId)).thenReturn(5L);

        // when
        GetUnreadNotificationCountResult result = getUnreadNotificationCountService.getUnreadCount(userId);

        // then
        assertThat(result.unreadCount()).isEqualTo(5L);
    }

    @Test
    @DisplayName("읽지 않은 알림이 없으면 unreadCount=0을 반환한다")
    void shouldReturnZeroWhenNoUnread() {
        // given
        Long userId = 1L;
        when(findNotificationPort.countUnreadByUserId(userId)).thenReturn(0L);

        // when
        GetUnreadNotificationCountResult result = getUnreadNotificationCountService.getUnreadCount(userId);

        // then
        assertThat(result.unreadCount()).isZero();
    }
}
