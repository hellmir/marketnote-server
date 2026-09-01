package com.personal.marketnote.notification.port.out.command;

import com.personal.marketnote.notification.domain.device.Platform;

public record SendPushNotificationCommand(
        String deviceToken,
        String title,
        String body,
        String url,
        Platform platform
) {
}
