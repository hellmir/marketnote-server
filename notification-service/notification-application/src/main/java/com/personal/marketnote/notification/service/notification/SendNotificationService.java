package com.personal.marketnote.notification.service.notification;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.notification.domain.device.DeviceToken;
import com.personal.marketnote.notification.domain.notification.*;
import com.personal.marketnote.notification.domain.preference.NotificationPreference;
import com.personal.marketnote.notification.domain.template.NotificationCategory;
import com.personal.marketnote.notification.domain.template.NotificationTemplate;
import com.personal.marketnote.notification.domain.template.NotificationTemplateNotFoundException;
import com.personal.marketnote.notification.domain.template.TemplateRenderer;
import com.personal.marketnote.notification.port.in.command.SendNotificationCommand;
import com.personal.marketnote.notification.port.in.result.notification.SendNotificationResult;
import com.personal.marketnote.notification.port.in.usecase.notification.SendNotificationUseCase;
import com.personal.marketnote.notification.port.out.command.SendPushNotificationCommand;
import com.personal.marketnote.notification.port.out.device.DeleteDeviceTokenPort;
import com.personal.marketnote.notification.port.out.device.FindDeviceTokenPort;
import com.personal.marketnote.notification.port.out.notification.FindNotificationPort;
import com.personal.marketnote.notification.port.out.notification.SaveNotificationPort;
import com.personal.marketnote.notification.port.out.notification.SendPushNotificationPort;
import com.personal.marketnote.notification.port.out.notification.UpdateNotificationPort;
import com.personal.marketnote.notification.port.out.preference.FindNotificationPreferencePort;
import com.personal.marketnote.notification.port.out.result.SendPushNotificationResult;
import com.personal.marketnote.notification.port.out.sse.PublishSseEventPort;
import com.personal.marketnote.notification.port.out.template.FindNotificationTemplatePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class SendNotificationService implements SendNotificationUseCase {

    private final FindNotificationTemplatePort findNotificationTemplatePort;
    private final FindNotificationPreferencePort findNotificationPreferencePort;
    private final FindDeviceTokenPort findDeviceTokenPort;
    private final SaveNotificationPort saveNotificationPort;
    private final UpdateNotificationPort updateNotificationPort;
    private final SendPushNotificationPort sendPushNotificationPort;
    private final DeleteDeviceTokenPort deleteDeviceTokenPort;
    private final FindNotificationPort findNotificationPort;
    private final PublishSseEventPort publishSseEventPort;
    private final Clock clock;

    @Override
    @Transactional(isolation = READ_COMMITTED)
    public SendNotificationResult sendNotification(SendNotificationCommand command) {
        NotificationTemplate template = findTemplate(command.templateCode());
        NotificationCategory category = template.getNotificationCategory();

        if (shouldSkipByConsent(category, command.userId(), template)) {
            SendNotificationResult skippedResult = saveSkippedNotification(command, template);
            publishUnreadCountChangedEvent(command.userId());
            return skippedResult;
        }

        String title = TemplateRenderer.render(template.getTitle(), command.variables());
        String body = TemplateRenderer.render(template.getBodyTemplate(), command.variables());
        String landingUrl = TemplateRenderer.render(template.getUrlTemplate(), command.variables());

        if (PromotionalNotificationPolicy.shouldApplyPolicy(category)) {
            title = PromotionalNotificationPolicy.applyAdLabel(title);
            body = PromotionalNotificationPolicy.applyOptOutGuide(body);
        }

        LocalDateTime now = LocalDateTime.now(clock);
        LocalDateTime scheduledAt = NightTimeNotificationPolicy.resolveScheduledAt(
                category, now, command.scheduledAt());

        DeliveryChannel deliveryChannel = DeliveryChannel.valueOf(command.deliveryChannel());
        Notification notification = createAndSaveNotification(
                command, template, title, body, landingUrl, deliveryChannel, scheduledAt);

        if (!notification.getSendStatus().isPending()) {
            publishUnreadCountChangedEvent(command.userId());
            return toResult(notification, 0, 0);
        }

        if (!deliveryChannel.hasPush()) {
            publishUnreadCountChangedEvent(command.userId());
            return toResult(notification, 0, 0);
        }

        SendNotificationResult result = sendPushNotifications(notification, title, body, landingUrl, command.userId());
        publishUnreadCountChangedEvent(command.userId());
        return result;
    }

    private NotificationTemplate findTemplate(String templateCode) {
        return findNotificationTemplatePort.findActiveByTemplateCode(templateCode)
                .orElseThrow(() -> new NotificationTemplateNotFoundException(templateCode));
    }

    private boolean shouldSkipByConsent(NotificationCategory category, Long userId,
                                         NotificationTemplate template) {
        if (!category.requiresConsent()) {
            return false;
        }
        Optional<NotificationPreference> preference =
                findNotificationPreferencePort.findByUserIdAndNotificationType(
                        userId, template.getNotificationType());
        return preference.isEmpty() || !preference.get().isEnabled();
    }

    private SendNotificationResult saveSkippedNotification(SendNotificationCommand command,
                                                            NotificationTemplate template) {
        DeliveryChannel deliveryChannel = DeliveryChannel.valueOf(command.deliveryChannel());
        NotificationCreateState state = NotificationCreateState.builder()
                .userId(command.userId())
                .notificationType(template.getNotificationType())
                .title(template.getTitle())
                .body(template.getBodyTemplate())
                .deliveryChannel(deliveryChannel)
                .build();

        Notification notification = Notification.from(state);
        notification.markAsSkipped();
        Notification saved = saveNotificationPort.save(notification);

        return toResult(saved, 0, 0);
    }

    private Notification createAndSaveNotification(SendNotificationCommand command,
                                                    NotificationTemplate template,
                                                    String title, String body, String landingUrl,
                                                    DeliveryChannel deliveryChannel,
                                                    LocalDateTime scheduledAt) {
        NotificationCreateState state = NotificationCreateState.builder()
                .userId(command.userId())
                .notificationType(template.getNotificationType())
                .title(title)
                .body(body)
                .deliveryChannel(deliveryChannel)
                .landingUrl(landingUrl)
                .scheduledAt(scheduledAt)
                .build();

        Notification notification = Notification.from(state);
        return saveNotificationPort.save(notification);
    }

    private SendNotificationResult sendPushNotifications(Notification notification,
                                                          String title, String body, String landingUrl,
                                                          Long userId) {
        List<DeviceToken> deviceTokens = findDeviceTokenPort.findActiveByUserId(userId);

        if (deviceTokens.isEmpty()) {
            notification.markAsFailed();
            updateNotificationPort.update(notification);
            return toResult(notification, 0, 0);
        }

        int sentCount = 0;
        int failedCount = 0;

        for (DeviceToken deviceToken : deviceTokens) {
            SendPushNotificationCommand pushCommand = new SendPushNotificationCommand(
                    deviceToken.getToken(), title, body, landingUrl, deviceToken.getPlatform());
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
                log.error("FCM 발송 중 예외 발생: userId={}, deviceId={}", userId, deviceToken.getDeviceId());
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

        return toResult(notification, sentCount, failedCount);
    }

    private void publishUnreadCountChangedEvent(Long userId) {
        long unreadCount = findNotificationPort.countUnreadByUserId(userId);
        publishSseEventPort.publish(userId, "UNREAD_COUNT_CHANGED",
                "{\"unreadCount\":" + unreadCount + "}");
    }

    private SendNotificationResult toResult(Notification notification, int sentCount, int failedCount) {
        return new SendNotificationResult(
                notification.getId(),
                notification.getSendStatus().name(),
                sentCount,
                failedCount
        );
    }
}
