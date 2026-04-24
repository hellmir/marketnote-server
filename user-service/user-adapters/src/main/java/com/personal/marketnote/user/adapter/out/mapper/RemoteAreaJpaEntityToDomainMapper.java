package com.personal.marketnote.user.adapter.out.mapper;

import com.personal.marketnote.user.adapter.out.persistence.remotearea.entity.RemoteAreaJpaEntity;
import com.personal.marketnote.user.domain.remotearea.RemoteArea;
import com.personal.marketnote.user.domain.remotearea.RemoteAreaSnapshotState;

public class RemoteAreaJpaEntityToDomainMapper {

    public static RemoteArea mapToDomain(RemoteAreaJpaEntity entity) {
        return RemoteArea.from(
                RemoteAreaSnapshotState.builder()
                        .id(entity.getId())
                        .province(entity.getProvince())
                        .district(entity.getDistrict())
                        .village(entity.getVillage())
                        .subarea(entity.getSubarea())
                        .build()
        );
    }
}
