package com.personal.marketnote.notification.adapter.out.persistence.device.repository;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.notification.adapter.out.persistence.device.entity.DeviceTokenJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface DeviceTokenJpaRepository extends JpaRepository<DeviceTokenJpaEntity, Long> {

    Optional<DeviceTokenJpaEntity> findByDeviceIdAndStatus(String deviceId, EntityStatus status);

    List<DeviceTokenJpaEntity> findByUserIdAndStatus(Long userId, EntityStatus status);

    List<DeviceTokenJpaEntity> findByUserIdInAndStatus(List<Long> userIds, EntityStatus status);

    @Modifying
    @Query("""
            UPDATE DeviceTokenJpaEntity d
            SET d.status = :inactiveStatus
            WHERE d.lastUsedAt < :threshold
              AND d.status = :activeStatus
            """)
    int deactivateStaleTokens(
            @Param("threshold") LocalDateTime threshold,
            @Param("activeStatus") EntityStatus activeStatus,
            @Param("inactiveStatus") EntityStatus inactiveStatus
    );
}
