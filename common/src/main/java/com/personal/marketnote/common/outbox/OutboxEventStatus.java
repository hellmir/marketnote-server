package com.personal.marketnote.common.outbox;

public enum OutboxEventStatus {
    PENDING,
    PUBLISHED,
    FAILED,
    DISCARDED;

    public boolean isPending() {
        return this == PENDING;
    }

    public boolean isPublished() {
        return this == PUBLISHED;
    }

    public boolean isFailed() {
        return this == FAILED;
    }

    public boolean isDiscarded() {
        return this == DISCARDED;
    }
}
