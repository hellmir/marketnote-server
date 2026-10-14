package com.personal.marketnote.notification.domain.aligo;

public enum AligoMessageType {

    AT("알림톡"),
    FT("브랜드메시지");

    private final String description;

    AligoMessageType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean isAlimTalk() {
        return this == AT;
    }

    public boolean isBrandTalk() {
        return this == FT;
    }
}
