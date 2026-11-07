package com.personal.marketnote.common.outbox.exception;

import com.personal.marketnote.common.domain.exception.DomainNotFoundException;

public class OutboxEventNotFoundException extends DomainNotFoundException {
    private static final String MESSAGE = "Outbox 이벤트를 찾을 수 없습니다.";

    public OutboxEventNotFoundException() {
        super(MESSAGE);
    }
}
