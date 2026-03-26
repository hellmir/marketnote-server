package com.personal.marketnote.common.saga;

public record SagaResponseMessage(
        String sagaId,
        String sagaType,
        String stepName,
        String messageType,
        boolean success,
        String response
) {
    public boolean isAction() {
        return SagaStepMessage.ACTION.equals(messageType);
    }

    public boolean isCompensation() {
        return SagaStepMessage.COMPENSATION.equals(messageType);
    }
}
