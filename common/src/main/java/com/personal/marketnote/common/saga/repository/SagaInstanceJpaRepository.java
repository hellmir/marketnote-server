package com.personal.marketnote.common.saga.repository;

import com.personal.marketnote.common.saga.SagaStatus;
import com.personal.marketnote.common.saga.entity.SagaInstanceJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SagaInstanceJpaRepository extends JpaRepository<SagaInstanceJpaEntity, Long> {

    Optional<SagaInstanceJpaEntity> findBySagaId(String sagaId);

    List<SagaInstanceJpaEntity> findBySagaTypeAndStatus(String sagaType, SagaStatus status);
}
