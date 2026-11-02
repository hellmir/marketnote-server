package com.personal.marketnote.common.outbox;

public enum OutboxResolutionAction {
    RETRY,
    DISCARD;

    public boolean isRetry() {
        return this == RETRY;
    }

    public boolean isDiscard() {
        return this == DISCARD;
    }
}
