package com.personal.marketnote.notification.port.out.notification;

import com.personal.marketnote.notification.domain.notification.Notification;

public interface SaveNotificationPort {
    Notification save(Notification notification);
}
