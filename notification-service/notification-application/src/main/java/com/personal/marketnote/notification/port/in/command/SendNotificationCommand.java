package com.personal.marketnote.notification.port.in.command;

import java.time.LocalDateTime;
import java.util.Map;

public record SendNotificationCommand(
        Long userId,
        String templateCode,
        Map<String, String> variables,
        String deliveryChannel,
        LocalDateTime scheduledAt
) {
}
