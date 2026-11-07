package com.personal.marketnote.user.adapter.out.persistence.remotearea;

import com.personal.marketnote.common.adapter.out.PersistenceAdapter;
import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.user.adapter.out.persistence.remotearea.entity.RemoteAreaJpaEntity;
import com.personal.marketnote.user.adapter.out.persistence.remotearea.mapper.RemoteAreaJpaEntityToDomainMapper;
import com.personal.marketnote.user.adapter.out.persistence.remotearea.repository.RemoteAreaJpaRepository;
import com.personal.marketnote.user.domain.remotearea.RemoteArea;
import com.personal.marketnote.user.exception.RemoteAreaAlreadyExistsException;
import com.personal.marketnote.user.exception.RemoteAreaNotFoundException;
import com.personal.marketnote.user.port.out.remotearea.DeleteRemoteAreaPort;
import com.personal.marketnote.user.port.out.remotearea.FindRemoteAreaPort;
import com.personal.marketnote.user.port.out.remotearea.SaveRemoteAreaPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;

@Slf4j
@PersistenceAdapter
@RequiredArgsConstructor
public class RemoteAreaPersistenceAdapter implements SaveRemoteAreaPort, FindRemoteAreaPort, DeleteRemoteAreaPort {

    private final RemoteAreaJpaRepository remoteAreaJpaRepository;

    @Override
    public void save(RemoteArea remoteArea) {
        RemoteAreaJpaEntity entity = RemoteAreaJpaEntity.from(remoteArea);
        try {
            remoteAreaJpaRepository.save(entity);
        } catch (DataIntegrityViolationException dive) {
            log.warn(
                    "이미 등록된 도서산간 지역입니다. province={}, district={}, village={}, subarea={}",
                    remoteArea.getProvince(), remoteArea.getDistrict(), remoteArea.getVillage(), remoteArea.getSubarea()
            );
            throw new RemoteAreaAlreadyExistsException();
        }
    }

    @Override
    public boolean existsByAddress(String province, String district, String village, String subarea) {
        return remoteAreaJpaRepository.existsByProvinceAndDistrictAndVillageAndSubareaAndStatus(
                province, district, village, subarea, EntityStatus.ACTIVE
        );
    }

    @Override
    public List<RemoteArea> findAllActive() {
        return remoteAreaJpaRepository.findAllByStatus(EntityStatus.ACTIVE).stream()
                .map(RemoteAreaJpaEntityToDomainMapper::mapToDomain)
                .toList();
    }

    @Override
    public Optional<RemoteArea> findActiveById(Long id) {
        return remoteAreaJpaRepository.findByIdAndStatus(id, EntityStatus.ACTIVE)
                .map(RemoteAreaJpaEntityToDomainMapper::mapToDomain);
    }

    @Override
    public void deactivate(RemoteArea remoteArea) {
        RemoteAreaJpaEntity entity = remoteAreaJpaRepository.findById(remoteArea.getId())
                .orElseThrow(() -> {
                    log.warn("도서산간 지역을 찾을 수 없습니다. remoteAreaId={}", remoteArea.getId());
                    return new RemoteAreaNotFoundException();
                });
        entity.markInactive();
    }
}
