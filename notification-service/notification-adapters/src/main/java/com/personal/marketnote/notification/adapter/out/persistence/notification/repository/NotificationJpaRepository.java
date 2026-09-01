package com.personal.marketnote.notification.adapter.out.persistence.notification.repository;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.notification.adapter.out.persistence.notification.entity.NotificationJpaEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationJpaRepository extends JpaRepository<NotificationJpaEntity, Long> {

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
}
