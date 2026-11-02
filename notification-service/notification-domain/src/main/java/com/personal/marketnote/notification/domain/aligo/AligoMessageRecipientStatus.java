package com.personal.marketnote.notification.domain.aligo;

public enum AligoMessageRecipientStatus {

    PENDING("대기"),
    SUCCESS("성공"),
    FAILED("실패");

    private final String description;

    AligoMessageRecipientStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean isPending() {
        return this == PENDING;
    }

    public boolean isSuccess() {
        return this == SUCCESS;
    }

    public boolean isFailed() {
        return this == FAILED;
    }
}
