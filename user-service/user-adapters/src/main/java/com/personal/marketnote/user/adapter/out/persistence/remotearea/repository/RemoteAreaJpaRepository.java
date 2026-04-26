package com.personal.marketnote.user.adapter.out.persistence.remotearea.repository;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.user.adapter.out.persistence.remotearea.entity.RemoteAreaJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RemoteAreaJpaRepository extends JpaRepository<RemoteAreaJpaEntity, Long> {

    boolean existsByProvinceAndDistrictAndVillageAndSubareaAndStatus(String province, String district, String village, String subarea, EntityStatus status);

    boolean existsByProvinceAndDistrictAndStatus(String province, String district, EntityStatus status);

    List<RemoteAreaJpaEntity> findAllByStatus(EntityStatus status);

    Optional<RemoteAreaJpaEntity> findByIdAndStatus(Long id, EntityStatus status);
}
