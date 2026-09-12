package com.personal.marketnote.notification.port.out.notification;

import java.time.LocalDateTime;

public interface DeleteNotificationPort {

    int deactivateExpiredNotifications(LocalDateTime threshold);
}
