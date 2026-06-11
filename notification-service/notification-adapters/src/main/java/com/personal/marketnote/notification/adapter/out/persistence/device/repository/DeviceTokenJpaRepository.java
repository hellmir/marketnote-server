package com.personal.marketnote.notification.adapter.out.persistence.device.repository;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.notification.adapter.out.persistence.device.entity.DeviceTokenJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DeviceTokenJpaRepository extends JpaRepository<DeviceTokenJpaEntity, Long> {

    Optional<DeviceTokenJpaEntity> findByDeviceIdAndStatus(String deviceId, EntityStatus status);
}
