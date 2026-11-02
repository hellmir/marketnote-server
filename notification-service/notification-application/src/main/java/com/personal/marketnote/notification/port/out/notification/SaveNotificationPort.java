package com.personal.marketnote.notification.port.out.notification;

import com.personal.marketnote.notification.domain.notification.Notification;

import java.util.List;

public interface SaveNotificationPort {
    Notification save(Notification notification);

    List<Notification> saveAll(List<Notification> notifications);
}
