package com.personal.marketnote.notification.port.in.command;

import java.time.LocalDateTime;

public record GetAligoMessageListCommand(
        int page,
        int limit,
        LocalDateTime startDate,
        LocalDateTime endDate
) {
}
