package com.personal.marketnote.common.saga.exception;

import com.personal.marketnote.common.domain.exception.DomainNotFoundException;

public class SagaStepNotFoundException extends DomainNotFoundException {
    private static final String MESSAGE = "SagaStep을 찾을 수 없습니다.";

    public SagaStepNotFoundException() {
        super(MESSAGE);
    }
}
