package com.personal.marketnote.notification.service.notification;

import com.personal.marketnote.notification.domain.device.DeviceToken;
import com.personal.marketnote.notification.domain.device.Platform;
import com.personal.marketnote.notification.domain.notification.*;
import com.personal.marketnote.notification.domain.template.NotificationType;
import com.personal.marketnote.notification.port.in.event.PushNotificationRequestedEvent;
import com.personal.marketnote.notification.port.out.command.SendPushNotificationCommand;
import com.personal.marketnote.notification.port.out.device.DeleteDeviceTokenPort;
import com.personal.marketnote.notification.port.out.device.FindDeviceTokenPort;
import com.personal.marketnote.notification.port.out.event.PublishNotificationSentEventPort;
import com.personal.marketnote.notification.port.out.notification.FindNotificationPort;
import com.personal.marketnote.notification.port.out.notification.SendPushNotificationPort;
import com.personal.marketnote.notification.port.out.notification.UpdateNotificationPort;
import com.personal.marketnote.notification.port.out.result.SendPushNotificationResult;
import com.personal.marketnote.notification.port.out.sse.NotificationSseEventType;
import com.personal.marketnote.notification.port.out.sse.PublishSseEventPort;
import com.personal.marketnote.notification.port.out.sse.payload.UnreadCountChangedPayload;
import com.personal.marketnote.notification.utility.VendorCommunicationRecorder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PushNotificationEventListenerTest {

    @InjectMocks
    private PushNotificationEventListener listener;

    @Mock
    private FindNotificationPort findNotificationPort;

    @Mock
    private FindDeviceTokenPort findDeviceTokenPort;

    @Mock
    private UpdateNotificationPort updateNotificationPort;

    @Mock
    private SendPushNotificationPort sendPushNotificationPort;

    @Mock
    private DeleteDeviceTokenPort deleteDeviceTokenPort;

    @Mock
    private PublishNotificationSentEventPort publishNotificationSentEventPort;

    @Mock
    private PublishSseEventPort publishSseEventPort;

    @Mock
    private VendorCommunicationRecorder vendorCommunicationRecorder;

    @Spy
    private Clock clock = Clock.fixed(
            Instant.parse("2026-06-03T05:00:00Z"),
            ZoneId.of("Asia/Seoul")
    );

    private static final Long NOTIFICATION_ID = 100L;
    private static final Long USER_ID = 1L;
    private static final String TITLE = "알림 제목";
    private static final String BODY = "알림 본문";
    private static final String LANDING_URL = "/test";

    private PushNotificationRequestedEvent event() {
        return new PushNotificationRequestedEvent(
                NOTIFICATION_ID, USER_ID, TITLE, BODY, LANDING_URL
        );
    }

    @Nested
    @DisplayName("알림 조회")
    class NotificationLookup {

        @Test
        @DisplayName("알림이 존재하지 않으면 NotificationNotFoundException이 발생한다")
        void shouldThrowWhenNotificationNotFound() {
            // given
            when(findNotificationPort.findActiveById(NOTIFICATION_ID))
                    .thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> listener.handlePushNotificationRequestedEvent(event()))
                    .isInstanceOf(NotificationNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("FCM 푸시 발송")
    class PushSending {

        @Test
        @DisplayName("2개 디바이스 모두 발송 성공하면 notification이 SENT 상태로 업데이트되고 Kafka 이벤트가 발행된다")
        void shouldMarkAsSentWhenAllDevicesSucceed() {
            // given
            Notification notification = createPendingNotification();
            when(findNotificationPort.findActiveById(NOTIFICATION_ID))
                    .thenReturn(Optional.of(notification));
            setupDeviceTokensAndPush(2, true);

            // when
            listener.handlePushNotificationRequestedEvent(event());

            // then
            assertThat(notification.getSendStatus()).isEqualTo(SendStatus.SENT);
            verify(updateNotificationPort).update(notification);
            verify(publishNotificationSentEventPort).publish(any());
        }

        @Test
        @DisplayName("2개 디바이스 중 1개 실패해도 SENT 상태로 업데이트되고 Kafka 이벤트가 발행된다")
        void shouldMarkAsSentWhenPartiallySucceeds() {
            // given
            Notification notification = createPendingNotification();
            when(findNotificationPort.findActiveById(NOTIFICATION_ID))
                    .thenReturn(Optional.of(notification));
            List<DeviceToken> tokens = List.of(
                    createDeviceToken(1L, "token1", Platform.ANDROID),
                    createDeviceToken(2L, "token2", Platform.IOS)
            );
            when(findDeviceTokenPort.findActiveByUserId(USER_ID)).thenReturn(tokens);
            when(sendPushNotificationPort.send(any(SendPushNotificationCommand.class)))
                    .thenReturn(SendPushNotificationResult.success("msg1"))
                    .thenReturn(SendPushNotificationResult.failure("INTERNAL"));

            // when
            listener.handlePushNotificationRequestedEvent(event());

            // then
            assertThat(notification.getSendStatus()).isEqualTo(SendStatus.SENT);
            verify(updateNotificationPort).update(notification);
            verify(publishNotificationSentEventPort).publish(any());
        }

        @Test
        @DisplayName("전부 실패하면 FAILED 상태로 업데이트되고 Kafka 이벤트는 발행되지 않는다")
        void shouldMarkAsFailedWhenAllFail() {
            // given
            Notification notification = createPendingNotification();
            when(findNotificationPort.findActiveById(NOTIFICATION_ID))
                    .thenReturn(Optional.of(notification));
            List<DeviceToken> tokens = List.of(createDeviceToken(1L, "token1", Platform.ANDROID));
            when(findDeviceTokenPort.findActiveByUserId(USER_ID)).thenReturn(tokens);
            when(sendPushNotificationPort.send(any(SendPushNotificationCommand.class)))
                    .thenReturn(SendPushNotificationResult.failure("INTERNAL"));

            // when
            listener.handlePushNotificationRequestedEvent(event());

            // then
            assertThat(notification.getSendStatus()).isEqualTo(SendStatus.FAILED);
            verify(updateNotificationPort).update(notification);
            verify(publishNotificationSentEventPort, never()).publish(any());
        }

        @Test
        @DisplayName("tokenInvalid 응답 시 해당 디바이스 토큰을 삭제한다")
        void shouldDeleteInvalidToken() {
            // given
            Notification notification = createPendingNotification();
            when(findNotificationPort.findActiveById(NOTIFICATION_ID))
                    .thenReturn(Optional.of(notification));
            List<DeviceToken> tokens = List.of(createDeviceToken(1L, "invalid-token", Platform.ANDROID));
            when(findDeviceTokenPort.findActiveByUserId(USER_ID)).thenReturn(tokens);
            when(sendPushNotificationPort.send(any(SendPushNotificationCommand.class)))
                    .thenReturn(SendPushNotificationResult.tokenInvalid("UNREGISTERED"));

            // when
            listener.handlePushNotificationRequestedEvent(event());

            // then
            verify(deleteDeviceTokenPort).deleteById(1L);
        }

        @Test
        @DisplayName("활성 디바이스 토큰이 없으면 FAILED 상태로 업데이트하고 FCM을 호출하지 않는다")
        void shouldMarkAsFailedWhenNoDeviceTokens() {
            // given
            Notification notification = createPendingNotification();
            when(findNotificationPort.findActiveById(NOTIFICATION_ID))
                    .thenReturn(Optional.of(notification));
            when(findDeviceTokenPort.findActiveByUserId(USER_ID)).thenReturn(List.of());

            // when
            listener.handlePushNotificationRequestedEvent(event());

            // then
            assertThat(notification.getSendStatus()).isEqualTo(SendStatus.FAILED);
            verify(updateNotificationPort).update(notification);
            verifyNoInteractions(sendPushNotificationPort);
        }
    }

    @Nested
    @DisplayName("FCM 벤더 통신 이력 기록")
    class VendorCommunicationRecording {

        @Test
        @DisplayName("푸시 발송 성공 시 디바이스 토큰 수만큼 recordFcmSendResult가 호출된다")
        void shouldRecordPerDeviceTokenOnSuccess() {
            // given
            Notification notification = createPendingNotification();
            when(findNotificationPort.findActiveById(NOTIFICATION_ID))
                    .thenReturn(Optional.of(notification));
            setupDeviceTokensAndPush(3, true);

            // when
            listener.handlePushNotificationRequestedEvent(event());

            // then
            verify(vendorCommunicationRecorder, times(3)).recordFcmSendResult(
                    any(Notification.class), any(DeviceToken.class),
                    anyString(), anyString(), anyString(),
                    argThat(SendPushNotificationResult::success)
            );
            verify(vendorCommunicationRecorder, never()).recordFcmSendException(
                    any(), any(), any(), any(), any(), any()
            );
        }

        @Test
        @DisplayName("푸시 발송 실패 시 recordFcmSendResult가 failure 결과로 호출된다")
        void shouldRecordFailureAsSendResult() {
            // given
            Notification notification = createPendingNotification();
            when(findNotificationPort.findActiveById(NOTIFICATION_ID))
                    .thenReturn(Optional.of(notification));
            List<DeviceToken> tokens = List.of(createDeviceToken(1L, "token1", Platform.ANDROID));
            when(findDeviceTokenPort.findActiveByUserId(USER_ID)).thenReturn(tokens);
            when(sendPushNotificationPort.send(any(SendPushNotificationCommand.class)))
                    .thenReturn(SendPushNotificationResult.failure("INTERNAL"));

            // when
            listener.handlePushNotificationRequestedEvent(event());

            // then
            verify(vendorCommunicationRecorder).recordFcmSendResult(
                    any(Notification.class), any(DeviceToken.class),
                    anyString(), anyString(), anyString(),
                    argThat(r -> !r.success() && "INTERNAL".equals(r.errorCode()) && !r.tokenInvalid())
            );
        }

        @Test
        @DisplayName("FCM 예외 발생 시 recordFcmSendException이 호출되고 다음 디바이스 토큰 발송을 계속한다")
        void shouldRecordAndContinueOnFcmException() {
            // given
            Notification notification = createPendingNotification();
            when(findNotificationPort.findActiveById(NOTIFICATION_ID))
                    .thenReturn(Optional.of(notification));
            List<DeviceToken> tokens = List.of(
                    createDeviceToken(1L, "token1", Platform.ANDROID),
                    createDeviceToken(2L, "token2", Platform.IOS)
            );
            when(findDeviceTokenPort.findActiveByUserId(USER_ID)).thenReturn(tokens);
            when(sendPushNotificationPort.send(any(SendPushNotificationCommand.class)))
                    .thenThrow(new FcmSendFailedException("타임아웃"))
                    .thenReturn(SendPushNotificationResult.success("msg2"));

            // when
            listener.handlePushNotificationRequestedEvent(event());

            // then
            assertThat(notification.getSendStatus()).isEqualTo(SendStatus.SENT);
            verify(vendorCommunicationRecorder).recordFcmSendException(
                    any(Notification.class),
                    argThat(dt -> dt.getId().equals(1L)),
                    anyString(), anyString(), anyString(),
                    any(FcmSendFailedException.class)
            );
            verify(vendorCommunicationRecorder).recordFcmSendResult(
                    any(Notification.class),
                    argThat(dt -> dt.getId().equals(2L)),
                    anyString(), anyString(), anyString(),
                    argThat(SendPushNotificationResult::success)
            );
        }

        @Test
        @DisplayName("tokenInvalid 응답 시 device_token 삭제와 함께 tokenInvalid=true 결과로 기록된다")
        void shouldRecordTokenInvalidWithDeletion() {
            // given
            Notification notification = createPendingNotification();
            when(findNotificationPort.findActiveById(NOTIFICATION_ID))
                    .thenReturn(Optional.of(notification));
            List<DeviceToken> tokens = List.of(createDeviceToken(1L, "invalid-token", Platform.ANDROID));
            when(findDeviceTokenPort.findActiveByUserId(USER_ID)).thenReturn(tokens);
            when(sendPushNotificationPort.send(any(SendPushNotificationCommand.class)))
                    .thenReturn(SendPushNotificationResult.tokenInvalid("UNREGISTERED"));

            // when
            listener.handlePushNotificationRequestedEvent(event());

            // then
            verify(deleteDeviceTokenPort).deleteById(1L);
            verify(vendorCommunicationRecorder).recordFcmSendResult(
                    any(Notification.class), any(DeviceToken.class),
                    anyString(), anyString(), anyString(),
                    argThat(r -> !r.success() && r.tokenInvalid() && "UNREGISTERED".equals(r.errorCode()))
            );
        }

        @Test
        @DisplayName("디바이스 토큰이 없으면 VendorCommunicationRecorder가 호출되지 않는다")
        void shouldNotRecordWhenNoDeviceTokens() {
            // given
            Notification notification = createPendingNotification();
            when(findNotificationPort.findActiveById(NOTIFICATION_ID))
                    .thenReturn(Optional.of(notification));
            when(findDeviceTokenPort.findActiveByUserId(USER_ID)).thenReturn(List.of());

            // when
            listener.handlePushNotificationRequestedEvent(event());

            // then
            verifyNoInteractions(vendorCommunicationRecorder);
        }
    }

    @Nested
    @DisplayName("Kafka Outbox 이벤트 발행")
    class KafkaEventPublish {

        @Test
        @DisplayName("SENT 상태로 전환되면 PushNotificationSentEvent를 발행한다")
        void shouldPublishSentEventWhenSent() {
            // given
            Notification notification = createPendingNotification();
            when(findNotificationPort.findActiveById(NOTIFICATION_ID))
                    .thenReturn(Optional.of(notification));
            setupDeviceTokensAndPush(1, true);

            // when
            listener.handlePushNotificationRequestedEvent(event());

            // then
            ArgumentCaptor<com.personal.marketnote.common.kafka.event.PushNotificationSentEvent> captor =
                    ArgumentCaptor.forClass(com.personal.marketnote.common.kafka.event.PushNotificationSentEvent.class);
            verify(publishNotificationSentEventPort).publish(captor.capture());
            com.personal.marketnote.common.kafka.event.PushNotificationSentEvent published = captor.getValue();
            assertThat(published.notificationId()).isEqualTo(NOTIFICATION_ID);
            assertThat(published.userId()).isEqualTo(USER_ID);
            assertThat(published.sentDeviceCount()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("SSE unreadCount 이벤트 발행")
    class SseEventPublish {

        @Test
        @DisplayName("FCM 처리 후 SSE unreadCount 이벤트를 발행한다")
        void shouldPublishUnreadCountEventAfterFcm() {
            // given
            Notification notification = createPendingNotification();
            when(findNotificationPort.findActiveById(NOTIFICATION_ID))
                    .thenReturn(Optional.of(notification));
            when(findNotificationPort.countUnreadByUserId(USER_ID)).thenReturn(5L);
            setupDeviceTokensAndPush(1, true);

            // when
            listener.handlePushNotificationRequestedEvent(event());

            // then
            verify(publishSseEventPort).publish(
                    eq(USER_ID),
                    eq(NotificationSseEventType.UNREAD_COUNT_CHANGED),
                    eq(UnreadCountChangedPayload.of(5L))
            );
        }

        @Test
        @DisplayName("SSE 발행 실패해도 예외가 전파되지 않는다")
        void shouldNotPropagateSseFailure() {
            // given
            Notification notification = createPendingNotification();
            when(findNotificationPort.findActiveById(NOTIFICATION_ID))
                    .thenReturn(Optional.of(notification));
            when(findNotificationPort.countUnreadByUserId(USER_ID))
                    .thenThrow(new RuntimeException("DB 오류"));
            setupDeviceTokensAndPush(1, true);

            // when & then
            listener.handlePushNotificationRequestedEvent(event());
        }
    }

    // --- Helper Methods ---

    private Notification createPendingNotification() {
        return Notification.from(
                NotificationSnapshotState.builder()
                        .id(NOTIFICATION_ID)
                        .userId(USER_ID)
                        .notificationType(NotificationType.ORDER_PAYMENT_COMPLETED)
                        .title(TITLE)
                        .body(BODY)
                        .deliveryChannel(DeliveryChannel.PUSH_ONLY)
                        .isRead(false)
                        .landingUrl(LANDING_URL)
                        .sendStatus(SendStatus.PENDING)
                        .status(com.personal.marketnote.common.domain.EntityStatus.ACTIVE)
                        .createdAt(LocalDateTime.now())
                        .modifiedAt(LocalDateTime.now())
                        .build()
        );
    }

    private void setupDeviceTokensAndPush(int count, boolean success) {
        List<DeviceToken> tokens = new java.util.ArrayList<>();
        for (int i = 1; i <= count; i++) {
            tokens.add(createDeviceToken((long) i, "token" + i, Platform.ANDROID));
        }
        when(findDeviceTokenPort.findActiveByUserId(USER_ID)).thenReturn(tokens);
        if (success) {
            when(sendPushNotificationPort.send(any(SendPushNotificationCommand.class)))
                    .thenReturn(SendPushNotificationResult.success("msg-id"));
        }
    }

    private DeviceToken createDeviceToken(Long id, String token, Platform platform) {
        return DeviceToken.from(
                com.personal.marketnote.notification.domain.device.DeviceTokenSnapshotState.builder()
                        .id(id)
                        .userId(USER_ID)
                        .token(token)
                        .platform(platform)
                        .deviceId("device-" + id)
                        .lastUsedAt(LocalDateTime.now())
                        .status(com.personal.marketnote.common.domain.EntityStatus.ACTIVE)
                        .createdAt(LocalDateTime.now())
                        .modifiedAt(LocalDateTime.now())
                        .build()
        );
    }
}
