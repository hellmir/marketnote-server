package com.personal.marketnote.common.saga.exception;

import com.personal.marketnote.common.domain.exception.DomainNotFoundException;

public class SagaInstanceNotFoundException extends DomainNotFoundException {
    private static final String MESSAGE = "SagaInstance를 찾을 수 없습니다.";

    public SagaInstanceNotFoundException() {
        super(MESSAGE);
    }
}
