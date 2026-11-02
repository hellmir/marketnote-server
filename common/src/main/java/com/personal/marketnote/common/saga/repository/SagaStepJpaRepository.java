package com.personal.marketnote.common.saga.repository;

import com.personal.marketnote.common.saga.entity.SagaStepJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SagaStepJpaRepository extends JpaRepository<SagaStepJpaEntity, Long> {

    List<SagaStepJpaEntity> findBySagaInstanceIdOrderByStepIndexAsc(Long sagaInstanceId);
}
