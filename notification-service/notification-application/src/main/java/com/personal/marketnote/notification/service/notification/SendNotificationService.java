package com.personal.marketnote.notification.service.notification;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.notification.domain.notification.*;
import com.personal.marketnote.notification.domain.preference.NotificationPreference;
import com.personal.marketnote.notification.domain.template.NotificationCategory;
import com.personal.marketnote.notification.domain.template.NotificationTemplate;
import com.personal.marketnote.notification.domain.template.NotificationTemplateNotFoundException;
import com.personal.marketnote.notification.domain.template.TemplateRenderer;
import com.personal.marketnote.notification.port.in.command.SendNotificationCommand;
import com.personal.marketnote.notification.port.in.event.PushNotificationRequestedEvent;
import com.personal.marketnote.notification.port.in.result.notification.SendNotificationResult;
import com.personal.marketnote.notification.port.in.usecase.notification.SendNotificationUseCase;
import com.personal.marketnote.notification.port.out.notification.FindNotificationPort;
import com.personal.marketnote.notification.port.out.notification.SaveNotificationPort;
import com.personal.marketnote.notification.port.out.preference.FindNotificationPreferencePort;
import com.personal.marketnote.notification.port.out.sse.PublishSseEventPort;
import com.personal.marketnote.notification.port.out.template.FindNotificationTemplatePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class SendNotificationService implements SendNotificationUseCase {

    private final FindNotificationTemplatePort findNotificationTemplatePort;
    private final FindNotificationPreferencePort findNotificationPreferencePort;
    private final SaveNotificationPort saveNotificationPort;
    private final FindNotificationPort findNotificationPort;
    private final PublishSseEventPort publishSseEventPort;
    private final ApplicationEventPublisher applicationEventPublisher;
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
                category, now, command.scheduledAt()
        );

        DeliveryChannel deliveryChannel = DeliveryChannel.valueOf(command.deliveryChannel());
        Notification notification = createAndSaveNotification(
                command, template, title, body, landingUrl, deliveryChannel, scheduledAt
        );

        if (!notification.getSendStatus().isPending()) {
            publishUnreadCountChangedEvent(command.userId());
            return toResult(notification);
        }

        if (!deliveryChannel.hasPush()) {
            publishUnreadCountChangedEvent(command.userId());
            return toResult(notification);
        }

        applicationEventPublisher.publishEvent(new PushNotificationRequestedEvent(
                notification.getId(), command.userId(), title, body, landingUrl
        ));

        return toResult(notification);
    }

    private NotificationTemplate findTemplate(String templateCode) {
        return findNotificationTemplatePort.findActiveByTemplateCode(templateCode)
                .orElseThrow(() -> new NotificationTemplateNotFoundException(templateCode));
    }

    private boolean shouldSkipByConsent(NotificationCategory category, Long userId, NotificationTemplate template) {
        if (!category.requiresConsent()) {
            return false;
        }
        Optional<NotificationPreference> preference =
                findNotificationPreferencePort.findByUserIdAndNotificationType(
                        userId, template.getNotificationType()
                );
        return preference.isEmpty() || !preference.get().isEnabled();
    }

    private SendNotificationResult saveSkippedNotification(
            SendNotificationCommand command, NotificationTemplate template
    ) {
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

        return toResult(saved);
    }

    private Notification createAndSaveNotification(
            SendNotificationCommand command, NotificationTemplate template,
            String title, String body, String landingUrl,
            DeliveryChannel deliveryChannel, LocalDateTime scheduledAt
    ) {
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

    private void publishUnreadCountChangedEvent(Long userId) {
        long unreadCount = findNotificationPort.countUnreadByUserId(userId);
        publishSseEventPort.publish(userId, "UNREAD_COUNT_CHANGED", "{\"unreadCount\":" + unreadCount + "}");
    }

    private SendNotificationResult toResult(Notification notification) {
        return new SendNotificationResult(notification.getId(), notification.getSendStatus().name());
    }
}
