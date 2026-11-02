package com.personal.marketnote.notification.domain.aligo;

public enum AligoTemplateType {

    BA("기본형"),
    EX("부가정보형"),
    AD("광고추가형"),
    MI("복합형");

    private final String description;

    AligoTemplateType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean isAdType() {
        return this == AD;
    }

    public static AligoTemplateType from(String code) {
        for (AligoTemplateType type : values()) {
            if (type.name().equals(code)) {
                return type;
            }
        }
        throw new InvalidAligoTemplateException("알 수 없는 templateType: " + code);
    }
}
