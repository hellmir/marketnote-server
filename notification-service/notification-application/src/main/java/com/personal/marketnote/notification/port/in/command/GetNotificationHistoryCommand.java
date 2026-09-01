package com.personal.marketnote.notification.port.in.command;

public record GetNotificationHistoryCommand(
        Long userId,
        Long cursor,
        int pageSize
) {
}
