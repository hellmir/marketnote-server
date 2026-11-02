package com.personal.marketnote.notification.mapper;

import com.personal.marketnote.notification.domain.template.NotificationCategory;
import com.personal.marketnote.notification.domain.template.NotificationTemplateCreateState;
import com.personal.marketnote.notification.domain.template.NotificationType;
import com.personal.marketnote.notification.port.in.command.RegisterNotificationTemplateCommand;

public class NotificationTemplateCommandToStateMapper {

    private NotificationTemplateCommandToStateMapper() {
    }

    public static NotificationTemplateCreateState mapToState(RegisterNotificationTemplateCommand command) {
        return NotificationTemplateCreateState.builder()
                .templateCode(command.templateCode())
                .notificationType(NotificationType.valueOf(command.notificationType()))
                .notificationCategory(NotificationCategory.valueOf(command.notificationCategory()))
                .title(command.title())
                .bodyTemplate(command.bodyTemplate())
                .urlTemplate(command.urlTemplate())
                .build();
    }
}
