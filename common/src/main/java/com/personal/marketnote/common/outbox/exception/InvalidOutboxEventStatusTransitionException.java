package com.personal.marketnote.common.outbox.exception;

import com.personal.marketnote.common.outbox.OutboxEventStatus;

public class InvalidOutboxEventStatusTransitionException extends IllegalStateException {
    public InvalidOutboxEventStatusTransitionException(OutboxEventStatus currentStatus) {
        super("현재 상태에서는 전이할 수 없습니다. 현재 상태: " + currentStatus);
    }
}
