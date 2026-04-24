package com.personal.marketnote.user.adapter.out.persistence.remotearea;

import com.personal.marketnote.common.adapter.out.PersistenceAdapter;
import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.user.adapter.out.persistence.remotearea.entity.RemoteAreaJpaEntity;
import com.personal.marketnote.user.adapter.out.persistence.remotearea.repository.RemoteAreaJpaRepository;
import com.personal.marketnote.user.domain.remotearea.RemoteArea;
import com.personal.marketnote.user.exception.RemoteAreaAlreadyExistsException;
import com.personal.marketnote.user.port.out.remotearea.FindRemoteAreaPort;
import com.personal.marketnote.user.port.out.remotearea.SaveRemoteAreaPort;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;

@PersistenceAdapter
@RequiredArgsConstructor
public class RemoteAreaPersistenceAdapter implements SaveRemoteAreaPort, FindRemoteAreaPort {

    private final RemoteAreaJpaRepository remoteAreaJpaRepository;

    @Override
    public void save(RemoteArea remoteArea) {
        RemoteAreaJpaEntity entity = RemoteAreaJpaEntity.from(remoteArea);
        try {
            remoteAreaJpaRepository.save(entity);
        } catch (DataIntegrityViolationException dive) {
            throw new RemoteAreaAlreadyExistsException(
                    remoteArea.getProvince(), remoteArea.getDistrict(), remoteArea.getVillage(), remoteArea.getSubarea()
            );
        }
    }

    @Override
    public boolean existsByAddress(String province, String district, String village, String subarea) {
        return remoteAreaJpaRepository.existsByProvinceAndDistrictAndVillageAndSubareaAndStatus(
                province, district, village, subarea, EntityStatus.ACTIVE
        );
    }
}
