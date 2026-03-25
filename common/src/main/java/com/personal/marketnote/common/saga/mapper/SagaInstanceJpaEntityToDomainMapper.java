package com.personal.marketnote.common.saga.mapper;

import com.personal.marketnote.common.saga.SagaInstance;
import com.personal.marketnote.common.saga.SagaInstanceSnapshotState;
import com.personal.marketnote.common.saga.entity.SagaInstanceJpaEntity;

public class SagaInstanceJpaEntityToDomainMapper {

    public static SagaInstance toDomain(SagaInstanceJpaEntity entity) {
        return SagaInstance.from(new SagaInstanceSnapshotState(
                entity.getId(),
                entity.getSagaId(),
                entity.getSagaType(),
                entity.getStatus(),
                entity.getCurrentStepIndex(),
                entity.getPayload(),
                entity.getCreatedAt(),
                entity.getModifiedAt(),
                entity.getCompletedAt()
        ));
    }
}
