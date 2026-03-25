package com.personal.marketnote.common.saga.adapter;

import com.personal.marketnote.common.adapter.out.PersistenceAdapter;
import com.personal.marketnote.common.saga.SagaInstance;
import com.personal.marketnote.common.saga.SagaStep;
import com.personal.marketnote.common.saga.entity.SagaInstanceJpaEntity;
import com.personal.marketnote.common.saga.entity.SagaStepJpaEntity;
import com.personal.marketnote.common.saga.exception.SagaInstanceNotFoundException;
import com.personal.marketnote.common.saga.exception.SagaStepNotFoundException;
import com.personal.marketnote.common.saga.mapper.SagaInstanceJpaEntityToDomainMapper;
import com.personal.marketnote.common.saga.mapper.SagaStepJpaEntityToDomainMapper;
import com.personal.marketnote.common.saga.port.FindSagaPort;
import com.personal.marketnote.common.saga.port.SaveSagaPort;
import com.personal.marketnote.common.saga.port.UpdateSagaPort;
import com.personal.marketnote.common.saga.repository.SagaInstanceJpaRepository;
import com.personal.marketnote.common.saga.repository.SagaStepJpaRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@PersistenceAdapter
@RequiredArgsConstructor
public class SagaPersistenceAdapter implements SaveSagaPort, FindSagaPort, UpdateSagaPort {

    private final SagaInstanceJpaRepository sagaInstanceJpaRepository;
    private final SagaStepJpaRepository sagaStepJpaRepository;

    @Override
    public SagaInstance save(SagaInstance instance) {
        SagaInstanceJpaEntity entity = SagaInstanceJpaEntity.from(instance);
        SagaInstanceJpaEntity saved = sagaInstanceJpaRepository.save(entity);
        return SagaInstanceJpaEntityToDomainMapper.toDomain(saved);
    }

    @Override
    public SagaStep saveStep(SagaStep step) {
        SagaStepJpaEntity entity = SagaStepJpaEntity.from(step);
        SagaStepJpaEntity saved = sagaStepJpaRepository.save(entity);
        return SagaStepJpaEntityToDomainMapper.toDomain(saved);
    }

    @Override
    public Optional<SagaInstance> findBySagaId(String sagaId) {
        return sagaInstanceJpaRepository.findBySagaId(sagaId)
                .map(SagaInstanceJpaEntityToDomainMapper::toDomain);
    }

    @Override
    public List<SagaStep> findStepsBySagaInstanceId(Long sagaInstanceId) {
        List<SagaStepJpaEntity> entities = sagaStepJpaRepository
                .findBySagaInstanceIdOrderByStepIndexAsc(sagaInstanceId);
        return SagaStepJpaEntityToDomainMapper.toDomainList(entities);
    }

    @Override
    public void update(SagaInstance instance) {
        SagaInstanceJpaEntity entity = sagaInstanceJpaRepository.findById(instance.getId())
                .orElseThrow(() -> new SagaInstanceNotFoundException(instance.getId()));
        entity.updateFrom(instance);
    }

    @Override
    public void updateStep(SagaStep step) {
        SagaStepJpaEntity entity = sagaStepJpaRepository.findById(step.getId())
                .orElseThrow(() -> new SagaStepNotFoundException(step.getId()));
        entity.updateFrom(step);
    }
}
