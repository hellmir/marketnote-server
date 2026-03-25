package com.personal.marketnote.common.saga.exception;

import com.personal.marketnote.common.saga.SagaStepStatus;

public class InvalidSagaStepStatusTransitionException extends IllegalStateException {
    public InvalidSagaStepStatusTransitionException(SagaStepStatus currentStatus) {
        super("현재 SagaStep 상태에서는 전이할 수 없습니다. 현재 상태: " + currentStatus);
    }
}
