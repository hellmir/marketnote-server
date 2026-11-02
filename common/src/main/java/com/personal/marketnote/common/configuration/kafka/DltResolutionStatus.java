package com.personal.marketnote.common.configuration.kafka;

public enum DltResolutionStatus {
    RETRIED,
    DISCARDED;

    public static final String UNRESOLVED = "UNRESOLVED";

    public boolean isRetried() {
        return this == RETRIED;
    }

    public boolean isDiscarded() {
        return this == DISCARDED;
    }

    public boolean isResolved() {
        return this == RETRIED || this == DISCARDED;
    }
}
