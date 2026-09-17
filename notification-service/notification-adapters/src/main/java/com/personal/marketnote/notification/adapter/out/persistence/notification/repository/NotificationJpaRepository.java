package com.personal.marketnote.notification.adapter.out.persistence.notification.repository;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.notification.adapter.out.persistence.notification.entity.NotificationJpaEntity;
import com.personal.marketnote.notification.domain.notification.SendStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface NotificationJpaRepository extends JpaRepository<NotificationJpaEntity, Long> {

    Optional<NotificationJpaEntity> findByIdAndStatus(Long id, EntityStatus status);

    @Query("""
            SELECT n FROM NotificationJpaEntity n
            WHERE n.userId = :userId
              AND n.status = :status
              AND (:cursor = -1L OR n.id < :cursor)
            ORDER BY n.id DESC
            """)
    List<NotificationJpaEntity> findByUserIdWithCursor(
            @Param("userId") Long userId,
            @Param("status") EntityStatus status,
            @Param("cursor") Long cursor,
            Pageable pageable
    );

    long countByUserIdAndStatus(Long userId, EntityStatus status);

    long countByUserIdAndStatusAndIsRead(Long userId, EntityStatus status, boolean isRead);

    @Query("""
            SELECT n FROM NotificationJpaEntity n
            WHERE n.sendStatus = :sendStatus
              AND n.scheduledAt <= :now
              AND n.status = :status
            ORDER BY n.scheduledAt ASC
            """)
    List<NotificationJpaEntity> findScheduledNotificationsDue(
            @Param("sendStatus") SendStatus sendStatus,
            @Param("now") LocalDateTime now,
            @Param("status") EntityStatus status
    );

    @Modifying
    @Query("""
            UPDATE NotificationJpaEntity n
            SET n.status = :inactiveStatus
            WHERE n.createdAt < :threshold
              AND n.status = :activeStatus
            """)
    int deactivateExpiredNotifications(
            @Param("threshold") LocalDateTime threshold,
            @Param("activeStatus") EntityStatus activeStatus,
            @Param("inactiveStatus") EntityStatus inactiveStatus
    );
}
