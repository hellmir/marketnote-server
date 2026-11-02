package com.personal.marketnote.notification.port.out.notification;

import com.personal.marketnote.notification.port.out.command.SendPushNotificationCommand;
import com.personal.marketnote.notification.port.out.result.SendBatchPushNotificationResult;
import com.personal.marketnote.notification.port.out.result.SendPushNotificationResult;

import java.util.List;

public interface SendPushNotificationPort {

    SendPushNotificationResult send(SendPushNotificationCommand command);

    SendBatchPushNotificationResult sendBatch(List<SendPushNotificationCommand> commands);
}
