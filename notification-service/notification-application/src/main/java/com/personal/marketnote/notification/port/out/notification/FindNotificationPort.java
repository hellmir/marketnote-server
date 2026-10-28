package com.personal.marketnote.notification.port.out.notification;

import com.personal.marketnote.notification.domain.notification.Notification;
import com.personal.marketnote.notification.domain.template.NotificationDisplayCategory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface FindNotificationPort {

    Optional<Notification> findActiveById(Long id);

    List<Notification> findByUserId(Long userId, Long cursor, int fetchSize, NotificationDisplayCategory category);

    long countByUserId(Long userId, NotificationDisplayCategory category);

    long countUnreadByUserId(Long userId);

    List<Notification> findScheduledNotificationsDue(LocalDateTime now);
}
