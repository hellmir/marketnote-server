package com.personal.marketnote.common.saga;

public enum SagaStepStatus {
    PENDING,
    PROCESSING,
    SUCCEEDED,
    FAILED,
    COMPENSATING,
    COMPENSATED;

    public boolean isPending() {
        return this == PENDING;
    }

    public boolean isProcessing() {
        return this == PROCESSING;
    }

    public boolean isSucceeded() {
        return this == SUCCEEDED;
    }

    public boolean isFailed() {
        return this == FAILED;
    }

    public boolean isCompensating() {
        return this == COMPENSATING;
    }

    public boolean isCompensated() {
        return this == COMPENSATED;
    }

    public boolean isTerminal() {
        return this == SUCCEEDED || this == COMPENSATED;
    }

    public boolean canProcess() {
        return this == PENDING;
    }

    public boolean canCompensate() {
        return this == SUCCEEDED || this == FAILED;
    }
}
