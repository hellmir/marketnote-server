package com.personal.marketnote.common.saga.mapper;

import com.personal.marketnote.common.saga.SagaStep;
import com.personal.marketnote.common.saga.SagaStepSnapshotState;
import com.personal.marketnote.common.saga.entity.SagaStepJpaEntity;

import java.util.List;

public class SagaStepJpaEntityToDomainMapper {

    public static SagaStep toDomain(SagaStepJpaEntity entity) {
        return SagaStep.from(new SagaStepSnapshotState(
                entity.getId(),
                entity.getSagaInstanceId(),
                entity.getStepName(),
                entity.getStepIndex(),
                entity.getStatus(),
                entity.getRequest(),
                entity.getResponse(),
                entity.getCompensationRequest(),
                entity.getCompensationResponse(),
                entity.getCreatedAt(),
                entity.getModifiedAt()
        ));
    }

    public static List<SagaStep> toDomainList(List<SagaStepJpaEntity> entities) {
        return entities.stream()
                .map(SagaStepJpaEntityToDomainMapper::toDomain)
                .toList();
    }
}
