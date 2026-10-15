package com.personal.marketnote.notification.domain.aligo;

public enum BrandTalkFailoverType {

    SMS("SMS"),
    LMS("LMS"),
    MMS("MMS");

    private final String description;

    BrandTalkFailoverType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean isMms() {
        return this == MMS;
    }

    public static BrandTalkFailoverType from(String code) {
        for (BrandTalkFailoverType type : values()) {
            if (type.name().equals(code)) {
                return type;
            }
        }
        throw new InvalidBrandTalkFailoverException("알 수 없는 대체 발송 유형입니다. code=" + code);
    }
}
