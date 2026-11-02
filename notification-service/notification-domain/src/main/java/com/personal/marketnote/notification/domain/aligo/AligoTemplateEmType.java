package com.personal.marketnote.notification.domain.aligo;

public enum AligoTemplateEmType {

    NONE("없음"),
    TEXT("강조 표기"),
    IMAGE("이미지");

    private final String description;

    AligoTemplateEmType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean isImage() {
        return this == IMAGE;
    }

    public static AligoTemplateEmType from(String code) {
        for (AligoTemplateEmType type : values()) {
            if (type.name().equals(code)) {
                return type;
            }
        }
        throw new InvalidAligoTemplateException("알 수 없는 templateEmType: " + code);
    }
}
