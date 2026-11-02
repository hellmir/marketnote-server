package com.personal.marketnote.common.saga;

public record SagaStepMessage(
        String sagaId,
        String sagaType,
        String stepName,
        String messageType,
        String payload
) {
    public static final String ACTION = "ACTION";
    public static final String COMPENSATION = "COMPENSATION";
}
