package com.personal.marketnote.notification.domain.aligo;

import java.util.Map;
import java.util.Set;

public enum AligoMessageStatus {

    REQUESTED("발송요청"),
    SENT("발송완료"),
    DELIVERED("수신확인"),
    FAILED("발송실패"),
    CANCELLED("취소");

    private static final Map<AligoMessageStatus, Set<AligoMessageStatus>> ALLOWED_TRANSITIONS = Map.of(
            REQUESTED, Set.of(SENT, FAILED, CANCELLED),
            SENT, Set.of(DELIVERED, FAILED),
            DELIVERED, Set.of(),
            FAILED, Set.of(),
            CANCELLED, Set.of()
    );

    private final String description;

    AligoMessageStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean canTransitionTo(AligoMessageStatus target) {
        return ALLOWED_TRANSITIONS.getOrDefault(this, Set.of()).contains(target);
    }

    public boolean isRequested() {
        return this == REQUESTED;
    }

    public boolean isSent() {
        return this == SENT;
    }

    public boolean isDelivered() {
        return this == DELIVERED;
    }

    public boolean isFailed() {
        return this == FAILED;
    }

    public boolean isCancelled() {
        return this == CANCELLED;
    }

    public boolean isTerminal() {
        return this == DELIVERED || this == FAILED || this == CANCELLED;
    }

    public boolean isCancellable() {
        return this == REQUESTED || this == SENT;
    }
}
