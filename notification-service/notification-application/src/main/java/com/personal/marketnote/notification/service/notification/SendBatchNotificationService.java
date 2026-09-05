package com.personal.marketnote.notification.service.notification;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.notification.domain.device.DeviceToken;
import com.personal.marketnote.notification.domain.notification.*;
import com.personal.marketnote.notification.domain.preference.NotificationPreference;
import com.personal.marketnote.notification.domain.template.NotificationCategory;
import com.personal.marketnote.notification.domain.template.NotificationTemplate;
import com.personal.marketnote.notification.domain.template.NotificationTemplateNotFoundException;
import com.personal.marketnote.notification.domain.template.TemplateRenderer;
import com.personal.marketnote.notification.port.in.command.SendBatchNotificationCommand;
import com.personal.marketnote.notification.port.in.result.notification.SendBatchNotificationResult;
import com.personal.marketnote.notification.port.in.usecase.notification.SendBatchNotificationUseCase;
import com.personal.marketnote.notification.port.out.command.SendPushNotificationCommand;
import com.personal.marketnote.notification.port.out.device.DeleteDeviceTokenPort;
import com.personal.marketnote.notification.port.out.device.FindDeviceTokenPort;
import com.personal.marketnote.notification.port.out.notification.SaveNotificationPort;
import com.personal.marketnote.notification.port.out.notification.SendPushNotificationPort;
import com.personal.marketnote.notification.port.out.notification.UpdateNotificationPort;
import com.personal.marketnote.notification.port.out.preference.FindNotificationPreferencePort;
import com.personal.marketnote.notification.port.out.result.SendBatchPushNotificationResult;
import com.personal.marketnote.notification.port.out.template.FindNotificationTemplatePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.notification.domain.notification.InvalidNotificationException;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class SendBatchNotificationService implements SendBatchNotificationUseCase {

    private static final int MAX_BATCH_SIZE = 10_000;

    private final FindNotificationTemplatePort findNotificationTemplatePort;
    private final FindNotificationPreferencePort findNotificationPreferencePort;
    private final FindDeviceTokenPort findDeviceTokenPort;
    private final SaveNotificationPort saveNotificationPort;
    private final UpdateNotificationPort updateNotificationPort;
    private final SendPushNotificationPort sendPushNotificationPort;
    private final DeleteDeviceTokenPort deleteDeviceTokenPort;
    private final Clock clock;

    @Override
    public SendBatchNotificationResult sendBatchNotification(SendBatchNotificationCommand command) {
        NotificationTemplate template = findTemplate(command.templateCode());
        NotificationCategory category = template.getNotificationCategory();
        List<Long> userIds = command.userIds().stream().distinct().toList();

        if (userIds.isEmpty()) {
            return emptyResult();
        }

        if (userIds.size() > MAX_BATCH_SIZE) {
            throw new InvalidNotificationException(
                    "대량 발송 대상은 최대 " + MAX_BATCH_SIZE + "명까지 가능합니다.");
        }

        Set<Long> consentedUserIds = resolveConsentedUserIds(category, userIds, template);
        Set<Long> skippedUserIds = userIds.stream()
                .filter(id -> !consentedUserIds.contains(id))
                .collect(Collectors.toSet());

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

        List<Notification> consentedNotifications = createNotifications(
                new ArrayList<>(consentedUserIds), template, title, body, landingUrl,
                deliveryChannel, scheduledAt);
        List<Notification> skippedNotifications = createSkippedNotifications(
                new ArrayList<>(skippedUserIds), template, deliveryChannel);

        List<Notification> allToSave = new ArrayList<>();
        allToSave.addAll(consentedNotifications);
        allToSave.addAll(skippedNotifications);

        List<Notification> savedNotifications = saveNotificationPort.saveAll(allToSave);

        List<Notification> savedConsented = savedNotifications.stream()
                .filter(n -> !n.getSendStatus().isSkipped())
                .toList();
        List<Notification> pendingNotifications = savedConsented.stream()
                .filter(n -> n.getSendStatus().isPending())
                .toList();

        if (pendingNotifications.isEmpty() || !deliveryChannel.hasPush()) {
            return buildResult(userIds.size(), 0, skippedUserIds.size(),
                    0, 0, 0);
        }

        return sendPushAndProcessResults(
                pendingNotifications, title, body, landingUrl,
                userIds.size(), skippedUserIds.size());
    }

    private NotificationTemplate findTemplate(String templateCode) {
        return findNotificationTemplatePort.findActiveByTemplateCode(templateCode)
                .orElseThrow(() -> new NotificationTemplateNotFoundException(templateCode));
    }

    private Set<Long> resolveConsentedUserIds(NotificationCategory category,
                                               List<Long> userIds,
                                               NotificationTemplate template) {
        if (!category.requiresConsent()) {
            return new HashSet<>(userIds);
        }
        List<NotificationPreference> enabledPrefs =
                findNotificationPreferencePort.findEnabledByUserIdsAndNotificationType(
                        userIds, template.getNotificationType());
        return enabledPrefs.stream()
                .map(NotificationPreference::getUserId)
                .collect(Collectors.toSet());
    }

    private List<Notification> createNotifications(List<Long> userIds,
                                                    NotificationTemplate template,
                                                    String title, String body, String landingUrl,
                                                    DeliveryChannel deliveryChannel,
                                                    LocalDateTime scheduledAt) {
        return userIds.stream()
                .map(userId -> {
                    NotificationCreateState state = NotificationCreateState.builder()
                            .userId(userId)
                            .notificationType(template.getNotificationType())
                            .title(title)
                            .body(body)
                            .deliveryChannel(deliveryChannel)
                            .landingUrl(landingUrl)
                            .scheduledAt(scheduledAt)
                            .build();
                    return Notification.from(state);
                })
                .toList();
    }

    private List<Notification> createSkippedNotifications(List<Long> userIds,
                                                            NotificationTemplate template,
                                                            DeliveryChannel deliveryChannel) {
        return userIds.stream()
                .map(userId -> {
                    NotificationCreateState state = NotificationCreateState.builder()
                            .userId(userId)
                            .notificationType(template.getNotificationType())
                            .title(template.getTitle())
                            .body(template.getBodyTemplate())
                            .deliveryChannel(deliveryChannel)
                            .build();
                    Notification notification = Notification.from(state);
                    notification.markAsSkipped();
                    return notification;
                })
                .toList();
    }

    private SendBatchNotificationResult sendPushAndProcessResults(
            List<Notification> pendingNotifications,
            String title, String body, String landingUrl,
            int totalUserCount, int skippedCount) {

        Map<Long, Notification> userIdToNotification = pendingNotifications.stream()
                .collect(Collectors.toMap(Notification::getUserId, n -> n));

        List<Long> pendingUserIds = new ArrayList<>(userIdToNotification.keySet());
        List<DeviceToken> deviceTokens = findDeviceTokenPort.findActiveByUserIds(pendingUserIds);

        Map<Long, List<DeviceToken>> tokensByUserId = deviceTokens.stream()
                .collect(Collectors.groupingBy(DeviceToken::getUserId));

        Set<Long> usersWithoutTokens = pendingUserIds.stream()
                .filter(userId -> !tokensByUserId.containsKey(userId))
                .collect(Collectors.toSet());

        for (Long userId : usersWithoutTokens) {
            Notification notification = userIdToNotification.get(userId);
            notification.markAsFailed();
        }

        List<SendPushNotificationCommand> pushCommands = new ArrayList<>();
        Map<String, DeviceToken> tokenMap = new HashMap<>();

        for (Map.Entry<Long, List<DeviceToken>> entry : tokensByUserId.entrySet()) {
            for (DeviceToken deviceToken : entry.getValue()) {
                SendPushNotificationCommand pushCommand = new SendPushNotificationCommand(
                        deviceToken.getToken(), title, body, landingUrl, deviceToken.getPlatform());
                pushCommands.add(pushCommand);
                tokenMap.put(deviceToken.getToken(), deviceToken);
            }
        }

        int sentDeviceCount = 0;
        int failedDeviceCount = 0;

        if (!pushCommands.isEmpty()) {
            SendBatchPushNotificationResult batchResult = sendPushNotificationPort.sendBatch(pushCommands);
            sentDeviceCount = batchResult.successCount();
            failedDeviceCount = batchResult.failureCount();

            Set<Long> failedTokenIds = processFailedTokens(batchResult.failedTokens(), tokenMap);

            Map<Long, Boolean> userSendSuccess = new HashMap<>();
            for (Map.Entry<Long, List<DeviceToken>> entry : tokensByUserId.entrySet()) {
                Long userId = entry.getKey();
                boolean anySuccess = entry.getValue().stream()
                        .anyMatch(dt -> !failedTokenIds.contains(dt.getId()));
                userSendSuccess.put(userId, anySuccess);
            }

            for (Map.Entry<Long, Boolean> entry : userSendSuccess.entrySet()) {
                Notification notification = userIdToNotification.get(entry.getKey());
                if (entry.getValue()) {
                    notification.markAsSent();
                    continue;
                }
                notification.markAsFailed();
            }
        }

        updateNotificationPort.updateAll(new ArrayList<>(userIdToNotification.values()));

        int sentUserCount = (int) userIdToNotification.values().stream()
                .filter(n -> n.getSendStatus().isSent())
                .count();
        int failedUserCount = (int) userIdToNotification.values().stream()
                .filter(n -> n.getSendStatus().isFailed())
                .count();

        return buildResult(totalUserCount, sentUserCount, skippedCount,
                failedUserCount, sentDeviceCount, failedDeviceCount);
    }

    private Set<Long> processFailedTokens(List<SendBatchPushNotificationResult.FailedToken> failedTokens,
                                            Map<String, DeviceToken> tokenMap) {
        Set<Long> failedTokenIds = new HashSet<>();
        for (SendBatchPushNotificationResult.FailedToken failed : failedTokens) {
            DeviceToken deviceToken = tokenMap.get(failed.deviceToken());
            if (FormatValidator.hasNoValue(deviceToken)) {
                continue;
            }
            failedTokenIds.add(deviceToken.getId());
            if (failed.tokenInvalid()) {
                deleteDeviceTokenPort.deleteById(deviceToken.getId());
            }
        }
        return failedTokenIds;
    }

    private SendBatchNotificationResult buildResult(int totalUserCount, int sentUserCount,
                                                      int skippedCount, int failedUserCount,
                                                      int sentDeviceCount, int failedDeviceCount) {
        return new SendBatchNotificationResult(
                totalUserCount, sentUserCount, skippedCount,
                failedUserCount, sentDeviceCount, failedDeviceCount);
    }

    private SendBatchNotificationResult emptyResult() {
        return new SendBatchNotificationResult(0, 0, 0, 0, 0, 0);
    }
}
