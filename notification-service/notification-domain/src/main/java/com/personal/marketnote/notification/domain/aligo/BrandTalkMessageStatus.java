package com.personal.marketnote.notification.domain.aligo;

import java.util.Map;
import java.util.Set;

public enum BrandTalkMessageStatus {

    REQUESTED("발송요청"),
    SENT("발송완료"),
    FAILED("발송실패");

    private static final Map<BrandTalkMessageStatus, Set<BrandTalkMessageStatus>> ALLOWED_TRANSITIONS = Map.of(
            REQUESTED, Set.of(SENT, FAILED),
            SENT, Set.of(),
            FAILED, Set.of()
    );

    private final String description;

    BrandTalkMessageStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean canTransitionTo(BrandTalkMessageStatus target) {
        return ALLOWED_TRANSITIONS.getOrDefault(this, Set.of()).contains(target);
    }

    public boolean isRequested() {
        return this == REQUESTED;
    }

    public boolean isSent() {
        return this == SENT;
    }

    public boolean isFailed() {
        return this == FAILED;
    }

    public boolean isTerminal() {
        return this == SENT || this == FAILED;
    }
}
