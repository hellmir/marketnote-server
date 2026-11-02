package com.personal.marketnote.common.outbox.exception;

public class OutboxResolveFailedException extends RuntimeException {
    public OutboxResolveFailedException(Long id) {
        super("Outbox 이벤트 해결 중 오류가 발생했습니다. id=" + id);
    }
}
