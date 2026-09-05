package com.personal.marketnote.notification.port.in.usecase.notification;

import com.personal.marketnote.notification.port.in.command.SendBatchNotificationCommand;
import com.personal.marketnote.notification.port.in.result.notification.SendBatchNotificationResult;

public interface SendBatchNotificationUseCase {

    SendBatchNotificationResult sendBatchNotification(SendBatchNotificationCommand command);
}
