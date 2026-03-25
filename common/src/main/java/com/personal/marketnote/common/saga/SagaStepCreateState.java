package com.personal.marketnote.common.saga;

public record SagaStepCreateState(
        Long sagaInstanceId,
        String stepName,
        int stepIndex,
        String request
) {
}
