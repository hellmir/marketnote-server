package com.personal.marketnote.notification.domain.aligo;

public enum AligoTemplateButtonLinkType {

    AC("채널추가"),
    DS("배송조회"),
    WL("웹링크"),
    AL("앱링크"),
    BK("봇키워드"),
    MD("메시지전달");

    private final String description;

    AligoTemplateButtonLinkType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean requiresWebLink() {
        return this == WL;
    }

    public boolean requiresAppLink() {
        return this == AL;
    }

    public static AligoTemplateButtonLinkType from(String code) {
        for (AligoTemplateButtonLinkType type : values()) {
            if (type.name().equals(code)) {
                return type;
            }
        }
        throw new InvalidAligoTemplateException("알 수 없는 buttonLinkType: " + code);
    }
}
