package com.personal.marketnote.common.saga;

public record SagaInstanceCreateState(
        String sagaId,
        String sagaType,
        String payload
) {
}
