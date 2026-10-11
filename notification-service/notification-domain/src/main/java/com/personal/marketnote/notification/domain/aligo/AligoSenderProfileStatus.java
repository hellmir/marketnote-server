package com.personal.marketnote.notification.domain.aligo;

public enum AligoSenderProfileStatus {

    A("활성"),
    S("중지"),
    D("삭제");

    private final String description;

    AligoSenderProfileStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean isActive() {
        return this == A;
    }

    public boolean isDeleted() {
        return this == D;
    }

    public static AligoSenderProfileStatus from(String code) {
        for (AligoSenderProfileStatus status : values()) {
            if (status.name().equals(code)) {
                return status;
            }
        }
        throw new InvalidAligoSenderProfileException("알 수 없는 senderProfileStatus: " + code);
    }
}
