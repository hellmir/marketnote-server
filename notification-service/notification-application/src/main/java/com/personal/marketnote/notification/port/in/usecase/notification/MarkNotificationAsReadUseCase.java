package com.personal.marketnote.notification.port.in.usecase.notification;

import com.personal.marketnote.notification.port.in.command.MarkNotificationAsReadCommand;

public interface MarkNotificationAsReadUseCase {
    void markAsRead(MarkNotificationAsReadCommand command);
}
