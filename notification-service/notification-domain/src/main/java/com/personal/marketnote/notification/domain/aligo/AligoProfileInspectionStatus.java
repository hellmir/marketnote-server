package com.personal.marketnote.notification.domain.aligo;

public enum AligoProfileInspectionStatus {

    A("승인"),
    C("신청"),
    B("차단"),
    E("삭제"),
    D("거부");

    private final String description;

    AligoProfileInspectionStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean isApproved() {
        return this == A;
    }

    public boolean isBlocked() {
        return this == B;
    }

    public boolean isDenied() {
        return this == D;
    }

    public static AligoProfileInspectionStatus from(String code) {
        for (AligoProfileInspectionStatus status : values()) {
            if (status.name().equals(code)) {
                return status;
            }
        }
        throw new InvalidAligoSenderProfileException("알 수 없는 profileInspectionStatus: " + code);
    }
}
