package com.personal.marketnote.common.configuration.kafka;

import com.personal.marketnote.common.domain.exception.DomainNotFoundException;

public class DltMessageNotFoundException extends DomainNotFoundException {
    private static final String MESSAGE = "DLT 메시지를 찾을 수 없습니다.";

    public DltMessageNotFoundException() {
        super(MESSAGE);
    }
}
