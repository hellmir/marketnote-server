package com.personal.marketnote.notification.adapter.out.persistence.device;

import com.personal.marketnote.common.adapter.out.PersistenceAdapter;
import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.notification.adapter.out.mapper.DeviceTokenJpaEntityToDomainMapper;
import com.personal.marketnote.notification.adapter.out.persistence.device.entity.DeviceTokenJpaEntity;
import com.personal.marketnote.notification.adapter.out.persistence.device.repository.DeviceTokenJpaRepository;
import com.personal.marketnote.notification.domain.device.DeviceToken;
import com.personal.marketnote.notification.domain.device.DeviceTokenNotFoundException;
import com.personal.marketnote.notification.domain.device.DuplicateDeviceTokenException;
import com.personal.marketnote.notification.port.out.device.DeleteDeviceTokenPort;
import com.personal.marketnote.notification.port.out.device.FindDeviceTokenPort;
import com.personal.marketnote.notification.port.out.device.SaveDeviceTokenPort;
import com.personal.marketnote.notification.port.out.device.UpdateDeviceTokenPort;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;

@PersistenceAdapter
@RequiredArgsConstructor
public class DeviceTokenPersistenceAdapter
        implements FindDeviceTokenPort, SaveDeviceTokenPort, UpdateDeviceTokenPort, DeleteDeviceTokenPort {

    private final DeviceTokenJpaRepository deviceTokenJpaRepository;

    @Override
    public List<DeviceToken> findActiveByUserId(Long userId) {
        return deviceTokenJpaRepository.findByUserIdAndStatus(userId, EntityStatus.ACTIVE).stream()
                .flatMap(entity -> DeviceTokenJpaEntityToDomainMapper.mapToDomain(entity).stream())
                .toList();
    }

    @Override
    public Optional<DeviceToken> findActiveByDeviceId(String deviceId) {
        return deviceTokenJpaRepository.findByDeviceIdAndStatus(deviceId, EntityStatus.ACTIVE)
                .flatMap(DeviceTokenJpaEntityToDomainMapper::mapToDomain);
    }

    @Override
    public Long save(DeviceToken deviceToken) {
        try {
            DeviceTokenJpaEntity entity = DeviceTokenJpaEntity.from(deviceToken);
            DeviceTokenJpaEntity saved = deviceTokenJpaRepository.save(entity);
            return saved.getId();
        } catch (DataIntegrityViolationException dive) {
            throw new DuplicateDeviceTokenException(deviceToken.getDeviceId());
        }
    }

    @Override
    public Long update(DeviceToken deviceToken) {
        DeviceTokenJpaEntity entity = deviceTokenJpaRepository.findById(deviceToken.getId())
                .orElseThrow(() -> new DeviceTokenNotFoundException(deviceToken.getId()));
        entity.updateFrom(deviceToken);
        return entity.getId();
    }

    @Override
    public void deleteById(Long id) {
        deviceTokenJpaRepository.deleteById(id);
        deviceTokenJpaRepository.flush();
    }
}
