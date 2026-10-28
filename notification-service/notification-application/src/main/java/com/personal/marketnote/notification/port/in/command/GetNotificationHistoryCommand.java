package com.personal.marketnote.notification.port.in.command;

import com.personal.marketnote.notification.domain.template.NotificationDisplayCategory;

public record GetNotificationHistoryCommand(
        Long userId,
        Long cursor,
        int pageSize,
        NotificationDisplayCategory category
) {
}
