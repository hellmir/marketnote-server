package com.personal.marketnote.notification.service.notification;

import com.personal.marketnote.common.kafka.event.PushNotificationSentEvent;
import com.personal.marketnote.notification.domain.device.DeviceToken;
import com.personal.marketnote.notification.domain.notification.FcmSendFailedException;
import com.personal.marketnote.notification.domain.notification.Notification;
import com.personal.marketnote.notification.domain.notification.NotificationNotFoundException;
import com.personal.marketnote.notification.port.in.event.PushNotificationRequestedEvent;
import com.personal.marketnote.notification.port.out.command.SendPushNotificationCommand;
import com.personal.marketnote.notification.port.out.device.DeleteDeviceTokenPort;
import com.personal.marketnote.notification.port.out.device.FindDeviceTokenPort;
import com.personal.marketnote.notification.port.out.event.PublishNotificationSentEventPort;
import com.personal.marketnote.notification.port.out.notification.FindNotificationPort;
import com.personal.marketnote.notification.port.out.notification.SendPushNotificationPort;
import com.personal.marketnote.notification.port.out.notification.UpdateNotificationPort;
import com.personal.marketnote.notification.port.out.result.SendPushNotificationResult;
import com.personal.marketnote.notification.port.out.sse.PublishSseEventPort;
import com.personal.marketnote.notification.utility.VendorCommunicationRecorder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;
import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

/**
 * 푸시 알림 발송 이벤트 리스너.
 *
 * <p>{@code SendNotificationService}가 Notification 저장 트랜잭션을 커밋한 뒤 AFTER_COMMIT 단계에서 호출되어
 * FCM 동기 HTTP 호출을 수행한다. 별도 트랜잭션(REQUIRES_NEW)에서 notification 상태 업데이트와
 * device token 정리, Kafka Outbox 이벤트 발행, SSE unreadCount 이벤트 발행을 처리한다.
 *
 * <p>트랜잭션과 FCM 호출을 분리하여 커넥션 풀 고갈을 방지한다.
 * FCM 실패 시에도 Notification 저장은 이미 커밋되었으므로 원자성 대신 관측성(로그/상태 업데이트)을 확보한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PushNotificationEventListener {

    private final FindNotificationPort findNotificationPort;
    private final FindDeviceTokenPort findDeviceTokenPort;
    private final UpdateNotificationPort updateNotificationPort;
    private final SendPushNotificationPort sendPushNotificationPort;
    private final DeleteDeviceTokenPort deleteDeviceTokenPort;
    private final PublishNotificationSentEventPort publishNotificationSentEventPort;
    private final PublishSseEventPort publishSseEventPort;
    private final VendorCommunicationRecorder vendorCommunicationRecorder;
    private final Clock clock;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = REQUIRES_NEW, isolation = READ_COMMITTED)
    public void handlePushNotificationRequestedEvent(PushNotificationRequestedEvent event) {
        Notification notification = findNotificationPort.findActiveById(event.notificationId())
                .orElseThrow(() -> new NotificationNotFoundException(event.notificationId()));

        List<DeviceToken> deviceTokens = findDeviceTokenPort.findActiveByUserId(event.userId());

        if (deviceTokens.isEmpty()) {
            notification.markAsFailed();
            updateNotificationPort.update(notification);
            publishUnreadCountChangedEvent(event.userId());
            return;
        }

        int sentCount = 0;
        int failedCount = 0;

        for (DeviceToken deviceToken : deviceTokens) {
            SendPushNotificationCommand pushCommand = new SendPushNotificationCommand(
                    deviceToken.getToken(), event.title(), event.body(), event.landingUrl(), deviceToken.getPlatform());
            try {
                SendPushNotificationResult pushResult = sendPushNotificationPort.send(pushCommand);
                vendorCommunicationRecorder.recordFcmSendResult(
                        notification, deviceToken, event.title(), event.body(), event.landingUrl(), pushResult
                );
                if (pushResult.success()) {
                    sentCount++;
                    continue;
                }
                failedCount++;
                if (pushResult.tokenInvalid()) {
                    deleteDeviceTokenPort.deleteById(deviceToken.getId());
                }
            } catch (FcmSendFailedException fsfe) {
                log.error("FCM 발송 중 예외 발생: userId={}, deviceId={}", event.userId(), deviceToken.getDeviceId());
                vendorCommunicationRecorder.recordFcmSendException(
                        notification, deviceToken, event.title(), event.body(), event.landingUrl(), fsfe
                );
                failedCount++;
            }
        }

        if (sentCount > 0) {
            notification.markAsSent();
        }
        if (sentCount == 0) {
            notification.markAsFailed();
        }
        updateNotificationPort.update(notification);

        if (notification.getSendStatus().isSent()) {
            publishNotificationSentEvent(notification, sentCount, failedCount);
        }

        publishUnreadCountChangedEvent(event.userId());
    }

    private void publishUnreadCountChangedEvent(Long userId) {
        try {
            long unreadCount = findNotificationPort.countUnreadByUserId(userId);
            publishSseEventPort.publish(userId, "UNREAD_COUNT_CHANGED", "{\"unreadCount\":" + unreadCount + "}");
        } catch (Exception e) {
            log.error("SSE unreadCount 이벤트 발행 실패 - userId={}", userId, e);
        }
    }

    private void publishNotificationSentEvent(Notification notification, int sentCount, int failedCount) {
        PushNotificationSentEvent event = new PushNotificationSentEvent(
                notification.getId(),
                notification.getUserId(),
                notification.getNotificationType().name(),
                notification.getSendStatus().name(),
                sentCount,
                failedCount,
                LocalDateTime.now(clock)
        );
        publishNotificationSentEventPort.publish(event);
    }
}
