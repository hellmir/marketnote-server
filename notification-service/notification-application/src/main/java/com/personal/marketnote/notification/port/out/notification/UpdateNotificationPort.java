package com.personal.marketnote.notification.port.out.notification;

import com.personal.marketnote.notification.domain.notification.Notification;

import java.util.List;

public interface UpdateNotificationPort {
    void update(Notification notification);

    void updateAll(List<Notification> notifications);
}
