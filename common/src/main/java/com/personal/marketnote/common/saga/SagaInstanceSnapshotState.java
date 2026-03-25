package com.personal.marketnote.common.saga;

import java.time.LocalDateTime;

public record SagaInstanceSnapshotState(
        Long id,
        String sagaId,
        String sagaType,
        SagaStatus status,
        int currentStepIndex,
        String payload,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt,
        LocalDateTime completedAt
) {
}
