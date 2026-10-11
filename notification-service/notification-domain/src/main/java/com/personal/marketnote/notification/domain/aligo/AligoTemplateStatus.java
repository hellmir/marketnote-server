package com.personal.marketnote.notification.domain.aligo;

public enum AligoTemplateStatus {

    S("중단"),
    A("정상"),
    R("대기");

    private final String description;

    AligoTemplateStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean isActive() {
        return this == A;
    }

    public boolean isStopped() {
        return this == S;
    }

    public boolean isWaiting() {
        return this == R;
    }

    public static AligoTemplateStatus from(String code) {
        for (AligoTemplateStatus status : values()) {
            if (status.name().equals(code)) {
                return status;
            }
        }
        throw new InvalidAligoTemplateException("알 수 없는 templateStatus: " + code);
    }
}
