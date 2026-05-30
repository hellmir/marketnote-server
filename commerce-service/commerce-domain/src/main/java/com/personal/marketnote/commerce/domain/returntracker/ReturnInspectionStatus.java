package com.personal.marketnote.commerce.domain.returntracker;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

@Getter
@RequiredArgsConstructor
public enum ReturnInspectionStatus {
    PENDING("검수 대기"),
    PASSED("검수 통과"),
    FAILED("검수 실패"),
    ON_HOLD("검수 보류");

    private final String description;

    private static final Map<ReturnInspectionStatus, Set<ReturnInspectionStatus>> ALLOWED_TRANSITIONS;

    static {
        ALLOWED_TRANSITIONS = new EnumMap<>(ReturnInspectionStatus.class);
        ALLOWED_TRANSITIONS.put(PENDING, Set.of(PASSED, FAILED, ON_HOLD));
        ALLOWED_TRANSITIONS.put(PASSED, Set.of());
        ALLOWED_TRANSITIONS.put(FAILED, Set.of());
        ALLOWED_TRANSITIONS.put(ON_HOLD, Set.of());
    }

    public boolean canTransitionTo(ReturnInspectionStatus target) {
        return ALLOWED_TRANSITIONS.getOrDefault(this, Set.of()).contains(target);
    }

    public boolean isPending() {
        return this == PENDING;
    }

    public boolean isPassed() {
        return this == PASSED;
    }

    public boolean isFailed() {
        return this == FAILED;
    }

    public boolean isOnHold() {
        return this == ON_HOLD;
    }
}
