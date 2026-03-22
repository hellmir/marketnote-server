package com.personal.marketnote.common.configuration.kafka;

public enum DltResolutionAction {
    RETRY,
    DISCARD;

    public boolean isRetry() {
        return this == RETRY;
    }

    public boolean isDiscard() {
        return this == DISCARD;
    }

    public DltResolutionStatus toResolutionStatus() {
        if (isRetry()) {
            return DltResolutionStatus.RETRIED;
        }
        return DltResolutionStatus.DISCARDED;
    }
}
