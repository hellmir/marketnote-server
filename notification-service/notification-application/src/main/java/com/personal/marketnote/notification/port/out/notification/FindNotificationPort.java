package com.personal.marketnote.notification.port.out.notification;

import com.personal.marketnote.notification.domain.notification.Notification;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface FindNotificationPort {

    Optional<Notification> findActiveById(Long id);

    List<Notification> findByUserId(Long userId, Long cursor, int fetchSize);

    long countByUserId(Long userId);

    long countUnreadByUserId(Long userId);

    List<Notification> findScheduledNotificationsDue(LocalDateTime now);
}
