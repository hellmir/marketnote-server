package com.personal.marketnote.common.saga.exception;

import com.personal.marketnote.common.domain.exception.DomainNotFoundException;

public class SagaDefinitionNotFoundException extends DomainNotFoundException {
    private static final String MESSAGE = "SagaDefinition을 찾을 수 없습니다.";

    public SagaDefinitionNotFoundException() {
        super(MESSAGE);
    }
}
