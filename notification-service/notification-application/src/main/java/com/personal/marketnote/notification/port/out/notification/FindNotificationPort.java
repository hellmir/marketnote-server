package com.personal.marketnote.notification.port.out.notification;

import com.personal.marketnote.notification.domain.notification.Notification;

import java.util.List;

public interface FindNotificationPort {

    List<Notification> findByUserId(Long userId, Long cursor, int fetchSize);

    long countByUserId(Long userId);
}
