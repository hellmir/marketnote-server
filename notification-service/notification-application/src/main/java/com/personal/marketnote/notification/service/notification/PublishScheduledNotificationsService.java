package com.personal.marketnote.notification.service.notification;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.notification.domain.device.DeviceToken;
import com.personal.marketnote.notification.domain.notification.FcmSendFailedException;
import com.personal.marketnote.notification.domain.notification.Notification;
import com.personal.marketnote.notification.port.in.usecase.notification.PublishScheduledNotificationsUseCase;
import com.personal.marketnote.notification.port.out.command.SendPushNotificationCommand;
import com.personal.marketnote.notification.port.out.device.DeleteDeviceTokenPort;
import com.personal.marketnote.notification.port.out.device.FindDeviceTokenPort;
import com.personal.marketnote.notification.port.out.notification.FindNotificationPort;
import com.personal.marketnote.notification.port.out.notification.SendPushNotificationPort;
import com.personal.marketnote.notification.port.out.notification.UpdateNotificationPort;
import com.personal.marketnote.notification.port.out.result.SendPushNotificationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class PublishScheduledNotificationsService implements PublishScheduledNotificationsUseCase {

    private final FindNotificationPort findNotificationPort;
    private final FindDeviceTokenPort findDeviceTokenPort;
    private final SendPushNotificationPort sendPushNotificationPort;
    private final UpdateNotificationPort updateNotificationPort;
    private final DeleteDeviceTokenPort deleteDeviceTokenPort;
    private final Clock clock;

    @Override
    public int publishScheduledNotifications() {
        LocalDateTime now = LocalDateTime.now(clock);
        List<Notification> scheduledNotifications = findNotificationPort.findScheduledNotificationsDue(now);

        if (scheduledNotifications.isEmpty()) {
            return 0;
        }

        int processedCount = 0;
        for (Notification notification : scheduledNotifications) {
            try {
                processScheduledNotification(notification);
                processedCount++;
            } catch (Exception e) {
                log.error("예약 알림 처리 실패: notificationId={}", notification.getId(), e);
            }
        }

        log.info("예약 알림 발송 완료: 총={}건, 처리={}건", scheduledNotifications.size(), processedCount);
        return processedCount;
    }

    private void processScheduledNotification(Notification notification) {
        notification.markAsPending();

        if (!notification.getDeliveryChannel().hasPush()) {
            updateNotificationPort.update(notification);
            return;
        }

        sendPushForNotification(notification);
        updateNotificationPort.update(notification);
    }

    private void sendPushForNotification(Notification notification) {
        List<DeviceToken> deviceTokens = findDeviceTokenPort.findActiveByUserId(notification.getUserId());

        if (deviceTokens.isEmpty()) {
            notification.markAsFailed();
            return;
        }

        int sentCount = 0;
        int failedCount = 0;

        for (DeviceToken deviceToken : deviceTokens) {
            SendPushNotificationCommand pushCommand = new SendPushNotificationCommand(
                    deviceToken.getToken(), notification.getTitle(), notification.getBody(),
                    notification.getLandingUrl(), deviceToken.getPlatform());
            try {
                SendPushNotificationResult pushResult = sendPushNotificationPort.send(pushCommand);
                if (pushResult.success()) {
                    sentCount++;
                    continue;
                }
                failedCount++;
                if (pushResult.tokenInvalid()) {
                    deleteDeviceTokenPort.deleteById(deviceToken.getId());
                }
            } catch (FcmSendFailedException fsfe) {
                log.error("FCM 발송 중 예외 발생: notificationId={}, deviceId={}",
                        notification.getId(), deviceToken.getDeviceId());
                failedCount++;
            }
        }

        if (sentCount > 0) {
            notification.markAsSent();
            return;
        }
        notification.markAsFailed();
    }
}
