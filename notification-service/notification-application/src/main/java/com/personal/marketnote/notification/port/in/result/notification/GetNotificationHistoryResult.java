package com.personal.marketnote.notification.port.in.result.notification;

import com.personal.marketnote.notification.domain.notification.Notification;

import java.util.List;

public record GetNotificationHistoryResult(
        Long totalElements,
        boolean hasNext,
        Long nextCursor,
        List<NotificationItemResult> notifications
) {
    public static GetNotificationHistoryResult from(Long totalElements,
                                                     boolean hasNext,
                                                     Long nextCursor,
                                                     List<Notification> notifications) {
        List<NotificationItemResult> items = notifications.stream()
                .map(NotificationItemResult::from)
                .toList();
        return new GetNotificationHistoryResult(totalElements, hasNext, nextCursor, items);
    }
}
