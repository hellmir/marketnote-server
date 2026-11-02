package com.personal.marketnote.notification.port.in.command;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record SendBatchNotificationCommand(
        List<Long> userIds,
        String templateCode,
        Map<String, String> variables,
        String deliveryChannel,
        LocalDateTime scheduledAt
) {
}
