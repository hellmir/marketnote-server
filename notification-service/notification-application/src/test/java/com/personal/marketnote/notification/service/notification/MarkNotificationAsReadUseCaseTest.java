package com.personal.marketnote.notification.service.notification;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.notification.domain.notification.*;
import com.personal.marketnote.notification.domain.template.NotificationType;
import com.personal.marketnote.notification.port.in.command.MarkNotificationAsReadCommand;
import com.personal.marketnote.notification.port.out.notification.FindNotificationPort;
import com.personal.marketnote.notification.port.out.notification.UpdateNotificationPort;
import com.personal.marketnote.notification.port.out.sse.PublishSseEventPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MarkNotificationAsReadUseCaseTest {

    @InjectMocks
    private MarkNotificationAsReadService markNotificationAsReadService;

    @Mock
    private FindNotificationPort findNotificationPort;

    @Mock
    private UpdateNotificationPort updateNotificationPort;

    @Mock
    private PublishSseEventPort publishSseEventPort;

    @Test
    @DisplayName("알림이 존재하고 본인 소유이면 읽음 처리한다")
    void shouldMarkAsReadWhenNotificationExistsAndOwnedByUser() {
        // given
        MarkNotificationAsReadCommand command = new MarkNotificationAsReadCommand(1L, 100L);
        Notification notification = createNotification(1L, 100L, false);
        when(findNotificationPort.findActiveById(1L)).thenReturn(Optional.of(notification));

        // when
        markNotificationAsReadService.markAsRead(command);

        // then
        verify(updateNotificationPort).update(notification);
    }

    @Test
    @DisplayName("알림이 존재하지 않으면 NotificationNotFoundException이 발생한다")
    void shouldThrowWhenNotificationNotFound() {
        // given
        MarkNotificationAsReadCommand command = new MarkNotificationAsReadCommand(1L, 100L);
        when(findNotificationPort.findActiveById(1L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> markNotificationAsReadService.markAsRead(command))
                .isInstanceOf(NotificationNotFoundException.class);
    }

    @Test
    @DisplayName("다른 사용자의 알림이면 NotificationNotFoundException이 발생한다")
    void shouldThrowWhenNotOwnedByUser() {
        // given
        MarkNotificationAsReadCommand command = new MarkNotificationAsReadCommand(1L, 100L);
        Notification notification = createNotification(1L, 999L, false);
        when(findNotificationPort.findActiveById(1L)).thenReturn(Optional.of(notification));

        // when & then
        assertThatThrownBy(() -> markNotificationAsReadService.markAsRead(command))
                .isInstanceOf(NotificationNotFoundException.class);
        verifyNoInteractions(updateNotificationPort);
    }

    @Test
    @DisplayName("이미 읽은 알림이면 업데이트를 수행하지 않는다")
    void shouldNotUpdateWhenAlreadyRead() {
        // given
        MarkNotificationAsReadCommand command = new MarkNotificationAsReadCommand(1L, 100L);
        Notification notification = createNotification(1L, 100L, true);
        when(findNotificationPort.findActiveById(1L)).thenReturn(Optional.of(notification));

        // when
        markNotificationAsReadService.markAsRead(command);

        // then
        verifyNoInteractions(updateNotificationPort);
    }

    private Notification createNotification(Long id, Long userId, boolean isRead) {
        return Notification.from(
                NotificationSnapshotState.builder()
                        .id(id)
                        .userId(userId)
                        .notificationType(NotificationType.ORDER_PAYMENT_COMPLETED)
                        .title("알림")
                        .body("알림 본문")
                        .deliveryChannel(DeliveryChannel.PUSH_AND_IN_APP)
                        .isRead(isRead)
                        .sendStatus(SendStatus.SENT)
                        .status(EntityStatus.ACTIVE)
                        .createdAt(LocalDateTime.now())
                        .modifiedAt(LocalDateTime.now())
                        .build()
        );
    }
}
