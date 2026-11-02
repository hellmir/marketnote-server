package com.personal.marketnote.notification.domain.aligo;

public enum BrandTalkTemplateStatus {

    NORMAL("O", "정상"),
    STOPPED("S", "중단"),
    WAITING("R", "대기");

    private final String code;
    private final String description;

    BrandTalkTemplateStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public boolean isNormal() {
        return this == NORMAL;
    }

    public boolean isStopped() {
        return this == STOPPED;
    }

    public boolean isWaiting() {
        return this == WAITING;
    }

    public static BrandTalkTemplateStatus from(String code) {
        for (BrandTalkTemplateStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new InvalidBrandTalkTemplateStatusException(code);
    }
}
