package com.personal.marketnote.notification.domain.notification;

public enum SendStatus {
    PENDING("대기"),
    SENT("발송 완료"),
    FAILED("발송 실패"),
    SKIPPED("건너뜀"),
    SCHEDULED("예약");

    private final String description;

    SendStatus(String description) {
        this.description = description;
    }

    public boolean isPending() {
        return this == PENDING;
    }

    public boolean isSent() {
        return this == SENT;
    }

    public boolean isFailed() {
        return this == FAILED;
    }

    public boolean isSkipped() {
        return this == SKIPPED;
    }

    public boolean isScheduled() {
        return this == SCHEDULED;
    }

    public String getDescription() {
        return description;
    }
}
