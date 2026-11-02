package com.personal.marketnote.common.saga;

import java.time.LocalDateTime;

public record SagaStepSnapshotState(
        Long id,
        Long sagaInstanceId,
        String stepName,
        int stepIndex,
        SagaStepStatus status,
        String request,
        String response,
        String compensationRequest,
        String compensationResponse,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt
) {
}
