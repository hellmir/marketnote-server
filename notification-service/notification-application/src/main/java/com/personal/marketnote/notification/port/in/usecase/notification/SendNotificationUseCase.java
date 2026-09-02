package com.personal.marketnote.notification.port.in.usecase.notification;

import com.personal.marketnote.notification.port.in.command.SendNotificationCommand;
import com.personal.marketnote.notification.port.in.result.notification.SendNotificationResult;

public interface SendNotificationUseCase {
    SendNotificationResult sendNotification(SendNotificationCommand command);
}
