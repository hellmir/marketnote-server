package com.personal.marketnote.common.saga.exception;

import com.personal.marketnote.common.saga.SagaStatus;

public class InvalidSagaStatusTransitionException extends IllegalStateException {
    public InvalidSagaStatusTransitionException(SagaStatus currentStatus) {
        super("현재 상태에서는 전이할 수 없습니다. 현재 상태: " + currentStatus);
    }
}
