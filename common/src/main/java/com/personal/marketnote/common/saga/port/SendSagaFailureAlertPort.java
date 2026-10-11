package com.personal.marketnote.common.saga.port;

import java.time.LocalDateTime;

public interface SendSagaFailureAlertPort {
    void sendFailureAlert(String sagaId, String sagaType, String failedStepName, String reason, LocalDateTime occurredAt);
}
