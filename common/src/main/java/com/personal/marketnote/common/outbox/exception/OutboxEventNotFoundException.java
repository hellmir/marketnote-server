package com.personal.marketnote.common.outbox.exception;

public class OutboxEventNotFoundException extends RuntimeException {
    public OutboxEventNotFoundException(Long id) {
        super("Outbox 이벤트를 찾을 수 없습니다. id=" + id);
    }
}
