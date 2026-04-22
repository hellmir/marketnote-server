package com.personal.marketnote.user.adapter.out.persistence.remotearea.repository;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.user.adapter.out.persistence.remotearea.entity.RemoteAreaJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RemoteAreaJpaRepository extends JpaRepository<RemoteAreaJpaEntity, Long> {

    Optional<RemoteAreaJpaEntity> findByZipCodeAndStatus(String zipCode, EntityStatus status);

    List<RemoteAreaJpaEntity> findAllByStatus(EntityStatus status);

    boolean existsByZipCodeAndStatus(String zipCode, EntityStatus status);
}
