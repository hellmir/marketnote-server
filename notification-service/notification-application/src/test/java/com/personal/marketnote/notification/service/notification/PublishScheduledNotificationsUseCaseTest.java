package com.personal.marketnote.notification.service.notification;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.notification.domain.device.DeviceToken;
import com.personal.marketnote.notification.domain.device.DeviceTokenSnapshotState;
import com.personal.marketnote.notification.domain.device.Platform;
import com.personal.marketnote.notification.domain.notification.DeliveryChannel;
import com.personal.marketnote.notification.domain.notification.Notification;
import com.personal.marketnote.notification.domain.notification.NotificationSnapshotState;
import com.personal.marketnote.notification.domain.notification.SendStatus;
import com.personal.marketnote.notification.domain.template.NotificationType;
import com.personal.marketnote.notification.port.out.command.SendPushNotificationCommand;
import com.personal.marketnote.notification.port.out.device.DeleteDeviceTokenPort;
import com.personal.marketnote.notification.port.out.device.FindDeviceTokenPort;
import com.personal.marketnote.notification.port.out.notification.FindNotificationPort;
import com.personal.marketnote.notification.port.out.notification.SendPushNotificationPort;
import com.personal.marketnote.notification.port.out.notification.UpdateNotificationPort;
import com.personal.marketnote.notification.port.out.result.SendPushNotificationResult;
import com.personal.marketnote.notification.port.out.sse.PublishSseEventPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PublishScheduledNotificationsUseCaseTest {

    @InjectMocks
    private PublishScheduledNotificationsService publishScheduledNotificationsService;

    @Mock
    private FindNotificationPort findNotificationPort;

    @Mock
    private FindDeviceTokenPort findDeviceTokenPort;

    @Mock
    private SendPushNotificationPort sendPushNotificationPort;

    @Mock
    private UpdateNotificationPort updateNotificationPort;

    @Mock
    private DeleteDeviceTokenPort deleteDeviceTokenPort;

    @Mock
    private PublishSseEventPort publishSseEventPort;

    @Spy
    private Clock clock = Clock.fixed(
            Instant.parse("2026-09-03T05:00:00Z"),
            ZoneId.of("Asia/Seoul")
    );

    @Nested
    @DisplayName("예약 알림 조회")
    class ScheduledNotificationQuery {

        @Test
        @DisplayName("예약 알림이 없으면 0을 반환한다")
        void shouldReturnZeroWhenNoScheduledNotifications() {
            // given
            when(findNotificationPort.findScheduledNotificationsDue(any(LocalDateTime.class)))
                    .thenReturn(List.of());

            // when
            int result = publishScheduledNotificationsService.publishScheduledNotifications();

            // then
            assertThat(result).isZero();
            verifyNoInteractions(findDeviceTokenPort);
            verifyNoInteractions(sendPushNotificationPort);
        }
    }

    @Nested
    @DisplayName("예약 알림 발송")
    class ScheduledNotificationPublishing {

        @Test
        @DisplayName("예약 시간이 도래한 PUSH_ONLY 알림을 FCM으로 발송하고 SENT 상태로 변경한다")
        void shouldSendScheduledPushNotification() {
            // given
            Notification notification = createScheduledNotification(1L, 100L, DeliveryChannel.PUSH_ONLY);
            when(findNotificationPort.findScheduledNotificationsDue(any(LocalDateTime.class)))
                    .thenReturn(List.of(notification));

            List<DeviceToken> tokens = List.of(createDeviceToken(10L, 100L, "token-1"));
            when(findDeviceTokenPort.findActiveByUserId(100L)).thenReturn(tokens);
            when(sendPushNotificationPort.send(any(SendPushNotificationCommand.class)))
                    .thenReturn(SendPushNotificationResult.success("msg-1"));

            // when
            int result = publishScheduledNotificationsService.publishScheduledNotifications();

            // then
            assertThat(result).isEqualTo(1);
            verify(updateNotificationPort).update(notification);
            assertThat(notification.getSendStatus()).isEqualTo(SendStatus.SENT);
        }

        @Test
        @DisplayName("IN_APP_ONLY 알림은 FCM 발송 없이 PENDING 상태로 변경한다")
        void shouldMarkInAppOnlyAsPending() {
            // given
            Notification notification = createScheduledNotification(2L, 200L, DeliveryChannel.IN_APP_ONLY);
            when(findNotificationPort.findScheduledNotificationsDue(any(LocalDateTime.class)))
                    .thenReturn(List.of(notification));

            // when
            int result = publishScheduledNotificationsService.publishScheduledNotifications();

            // then
            assertThat(result).isEqualTo(1);
            verify(updateNotificationPort).update(notification);
            assertThat(notification.getSendStatus()).isEqualTo(SendStatus.PENDING);
            verifyNoInteractions(findDeviceTokenPort);
            verifyNoInteractions(sendPushNotificationPort);
        }

        @Test
        @DisplayName("디바이스 토큰이 없으면 FAILED 상태로 변경한다")
        void shouldMarkAsFailedWhenNoDeviceTokens() {
            // given
            Notification notification = createScheduledNotification(3L, 300L, DeliveryChannel.PUSH_ONLY);
            when(findNotificationPort.findScheduledNotificationsDue(any(LocalDateTime.class)))
                    .thenReturn(List.of(notification));
            when(findDeviceTokenPort.findActiveByUserId(300L)).thenReturn(List.of());

            // when
            int result = publishScheduledNotificationsService.publishScheduledNotifications();

            // then
            assertThat(result).isEqualTo(1);
            assertThat(notification.getSendStatus()).isEqualTo(SendStatus.FAILED);
        }

        @Test
        @DisplayName("FCM 발송 실패 시 FAILED 상태로 변경한다")
        void shouldMarkAsFailedOnFcmFailure() {
            // given
            Notification notification = createScheduledNotification(4L, 400L, DeliveryChannel.PUSH_ONLY);
            when(findNotificationPort.findScheduledNotificationsDue(any(LocalDateTime.class)))
                    .thenReturn(List.of(notification));

            List<DeviceToken> tokens = List.of(createDeviceToken(40L, 400L, "token-4"));
            when(findDeviceTokenPort.findActiveByUserId(400L)).thenReturn(tokens);
            when(sendPushNotificationPort.send(any(SendPushNotificationCommand.class)))
                    .thenReturn(SendPushNotificationResult.failure("INTERNAL"));

            // when
            publishScheduledNotificationsService.publishScheduledNotifications();

            // then
            assertThat(notification.getSendStatus()).isEqualTo(SendStatus.FAILED);
        }

        @Test
        @DisplayName("tokenInvalid 토큰은 삭제한다")
        void shouldDeleteInvalidTokens() {
            // given
            Notification notification = createScheduledNotification(5L, 500L, DeliveryChannel.PUSH_ONLY);
            when(findNotificationPort.findScheduledNotificationsDue(any(LocalDateTime.class)))
                    .thenReturn(List.of(notification));

            List<DeviceToken> tokens = List.of(createDeviceToken(50L, 500L, "invalid-token"));
            when(findDeviceTokenPort.findActiveByUserId(500L)).thenReturn(tokens);
            when(sendPushNotificationPort.send(any(SendPushNotificationCommand.class)))
                    .thenReturn(SendPushNotificationResult.tokenInvalid("UNREGISTERED"));

            // when
            publishScheduledNotificationsService.publishScheduledNotifications();

            // then
            verify(deleteDeviceTokenPort).deleteById(50L);
        }

        @Test
        @DisplayName("여러 예약 알림을 순차 처리하고 처리 건수를 반환한다")
        void shouldProcessMultipleScheduledNotifications() {
            // given
            Notification n1 = createScheduledNotification(1L, 100L, DeliveryChannel.PUSH_ONLY);
            Notification n2 = createScheduledNotification(2L, 200L, DeliveryChannel.PUSH_ONLY);
            Notification n3 = createScheduledNotification(3L, 300L, DeliveryChannel.IN_APP_ONLY);
            when(findNotificationPort.findScheduledNotificationsDue(any(LocalDateTime.class)))
                    .thenReturn(List.of(n1, n2, n3));

            when(findDeviceTokenPort.findActiveByUserId(100L))
                    .thenReturn(List.of(createDeviceToken(10L, 100L, "token-1")));
            when(findDeviceTokenPort.findActiveByUserId(200L))
                    .thenReturn(List.of(createDeviceToken(20L, 200L, "token-2")));
            when(sendPushNotificationPort.send(any(SendPushNotificationCommand.class)))
                    .thenReturn(SendPushNotificationResult.success("msg-id"));

            // when
            int result = publishScheduledNotificationsService.publishScheduledNotifications();

            // then
            assertThat(result).isEqualTo(3);
            verify(updateNotificationPort, times(3)).update(any(Notification.class));
        }

        @Test
        @DisplayName("개별 알림 처리 중 예외가 발생해도 나머지 알림은 계속 처리한다")
        void shouldContinueProcessingOnIndividualFailure() {
            // given
            Notification n1 = createScheduledNotification(1L, 100L, DeliveryChannel.PUSH_ONLY);
            Notification n2 = createScheduledNotification(2L, 200L, DeliveryChannel.PUSH_ONLY);
            when(findNotificationPort.findScheduledNotificationsDue(any(LocalDateTime.class)))
                    .thenReturn(List.of(n1, n2));

            when(findDeviceTokenPort.findActiveByUserId(100L))
                    .thenThrow(new RuntimeException("DB error"));
            when(findDeviceTokenPort.findActiveByUserId(200L))
                    .thenReturn(List.of(createDeviceToken(20L, 200L, "token-2")));
            when(sendPushNotificationPort.send(any(SendPushNotificationCommand.class)))
                    .thenReturn(SendPushNotificationResult.success("msg-id"));

            // when
            int result = publishScheduledNotificationsService.publishScheduledNotifications();

            // then
            assertThat(result).isEqualTo(1);
            assertThat(n2.getSendStatus()).isEqualTo(SendStatus.SENT);
        }
    }

    @Nested
    @DisplayName("SSE unreadCount 갱신 이벤트 발행")
    class SseEventPublishing {

        @Test
        @DisplayName("PUSH 예약 알림 발송 후 SSE unreadCount 갱신 이벤트를 발행한다")
        void shouldPublishSseEventAfterScheduledPushSent() {
            // given
            Notification notification = createScheduledNotification(1L, 100L, DeliveryChannel.PUSH_ONLY);
            when(findNotificationPort.findScheduledNotificationsDue(any(LocalDateTime.class)))
                    .thenReturn(List.of(notification));

            List<DeviceToken> tokens = List.of(createDeviceToken(10L, 100L, "token-1"));
            when(findDeviceTokenPort.findActiveByUserId(100L)).thenReturn(tokens);
            when(sendPushNotificationPort.send(any(SendPushNotificationCommand.class)))
                    .thenReturn(SendPushNotificationResult.success("msg-1"));
            when(findNotificationPort.countUnreadByUserId(100L)).thenReturn(3L);

            // when
            publishScheduledNotificationsService.publishScheduledNotifications();

            // then
            verify(publishSseEventPort).publish(100L, "UNREAD_COUNT_CHANGED", "{\"unreadCount\":3}");
        }

        @Test
        @DisplayName("IN_APP_ONLY 예약 알림 처리 후에도 SSE unreadCount 갱신 이벤트를 발행한다")
        void shouldPublishSseEventForInAppOnlyScheduledNotification() {
            // given
            Notification notification = createScheduledNotification(2L, 200L, DeliveryChannel.IN_APP_ONLY);
            when(findNotificationPort.findScheduledNotificationsDue(any(LocalDateTime.class)))
                    .thenReturn(List.of(notification));
            when(findNotificationPort.countUnreadByUserId(200L)).thenReturn(1L);

            // when
            publishScheduledNotificationsService.publishScheduledNotifications();

            // then
            verify(publishSseEventPort).publish(200L, "UNREAD_COUNT_CHANGED", "{\"unreadCount\":1}");
        }

        @Test
        @DisplayName("여러 예약 알림 처리 시 각각의 userId에 대해 SSE 이벤트를 발행한다")
        void shouldPublishSseEventForEachScheduledNotification() {
            // given
            Notification n1 = createScheduledNotification(1L, 100L, DeliveryChannel.PUSH_ONLY);
            Notification n2 = createScheduledNotification(2L, 200L, DeliveryChannel.IN_APP_ONLY);
            when(findNotificationPort.findScheduledNotificationsDue(any(LocalDateTime.class)))
                    .thenReturn(List.of(n1, n2));

            when(findDeviceTokenPort.findActiveByUserId(100L))
                    .thenReturn(List.of(createDeviceToken(10L, 100L, "token-1")));
            when(sendPushNotificationPort.send(any(SendPushNotificationCommand.class)))
                    .thenReturn(SendPushNotificationResult.success("msg-id"));
            when(findNotificationPort.countUnreadByUserId(100L)).thenReturn(5L);
            when(findNotificationPort.countUnreadByUserId(200L)).thenReturn(2L);

            // when
            publishScheduledNotificationsService.publishScheduledNotifications();

            // then
            verify(publishSseEventPort).publish(100L, "UNREAD_COUNT_CHANGED", "{\"unreadCount\":5}");
            verify(publishSseEventPort).publish(200L, "UNREAD_COUNT_CHANGED", "{\"unreadCount\":2}");
        }

        @Test
        @DisplayName("예약 알림이 없으면 SSE 이벤트를 발행하지 않는다")
        void shouldNotPublishSseEventWhenNoScheduledNotifications() {
            // given
            when(findNotificationPort.findScheduledNotificationsDue(any(LocalDateTime.class)))
                    .thenReturn(List.of());

            // when
            publishScheduledNotificationsService.publishScheduledNotifications();

            // then
            verifyNoInteractions(publishSseEventPort);
        }

        @Test
        @DisplayName("FCM 발송 실패해도 SSE unreadCount 갱신 이벤트를 발행한다")
        void shouldPublishSseEventEvenWhenPushFails() {
            // given
            Notification notification = createScheduledNotification(4L, 400L, DeliveryChannel.PUSH_ONLY);
            when(findNotificationPort.findScheduledNotificationsDue(any(LocalDateTime.class)))
                    .thenReturn(List.of(notification));

            List<DeviceToken> tokens = List.of(createDeviceToken(40L, 400L, "token-4"));
            when(findDeviceTokenPort.findActiveByUserId(400L)).thenReturn(tokens);
            when(sendPushNotificationPort.send(any(SendPushNotificationCommand.class)))
                    .thenReturn(SendPushNotificationResult.failure("INTERNAL"));
            when(findNotificationPort.countUnreadByUserId(400L)).thenReturn(7L);

            // when
            publishScheduledNotificationsService.publishScheduledNotifications();

            // then
            verify(publishSseEventPort).publish(400L, "UNREAD_COUNT_CHANGED", "{\"unreadCount\":7}");
        }
    }

    // --- Helper Methods ---

    private Notification createScheduledNotification(Long id, Long userId, DeliveryChannel channel) {
        return Notification.from(
                NotificationSnapshotState.builder()
                        .id(id)
                        .userId(userId)
                        .notificationType(NotificationType.ORDER_PAYMENT_COMPLETED)
                        .title("예약 알림")
                        .body("예약 본문")
                        .deliveryChannel(channel)
                        .isRead(false)
                        .landingUrl("/test")
                        .sendStatus(SendStatus.SCHEDULED)
                        .scheduledAt(LocalDateTime.of(2026, 4, 10, 8, 0))
                        .status(EntityStatus.ACTIVE)
                        .createdAt(LocalDateTime.now())
                        .modifiedAt(LocalDateTime.now())
                        .build()
        );
    }

    private DeviceToken createDeviceToken(Long id, Long userId, String token) {
        return DeviceToken.from(
                DeviceTokenSnapshotState.builder()
                        .id(id)
                        .userId(userId)
                        .token(token)
                        .platform(Platform.ANDROID)
                        .deviceId("device-" + id)
                        .lastUsedAt(LocalDateTime.now())
                        .status(EntityStatus.ACTIVE)
                        .createdAt(LocalDateTime.now())
                        .modifiedAt(LocalDateTime.now())
                        .build()
        );
    }
}
