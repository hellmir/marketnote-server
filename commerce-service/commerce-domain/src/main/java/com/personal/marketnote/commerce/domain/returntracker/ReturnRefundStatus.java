package com.personal.marketnote.commerce.domain.returntracker;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

@Getter
@RequiredArgsConstructor
public enum ReturnRefundStatus {
    PENDING("환불 대기"),
    COMPLETED("환불 완료"),
    FAILED("환불 실패");

    private final String description;

    private static final Map<ReturnRefundStatus, Set<ReturnRefundStatus>> ALLOWED_TRANSITIONS;

    static {
        ALLOWED_TRANSITIONS = new EnumMap<>(ReturnRefundStatus.class);
        ALLOWED_TRANSITIONS.put(PENDING, Set.of(COMPLETED, FAILED));
        ALLOWED_TRANSITIONS.put(COMPLETED, Set.of());
        ALLOWED_TRANSITIONS.put(FAILED, Set.of(PENDING));
    }

    public boolean canTransitionTo(ReturnRefundStatus target) {
        return ALLOWED_TRANSITIONS.getOrDefault(this, Set.of()).contains(target);
    }

    public boolean isPending() {
        return this == PENDING;
    }

    public boolean isCompleted() {
        return this == COMPLETED;
    }

    public boolean isFailed() {
        return this == FAILED;
    }
}
