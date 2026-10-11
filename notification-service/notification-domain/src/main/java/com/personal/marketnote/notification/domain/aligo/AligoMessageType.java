package com.personal.marketnote.notification.domain.aligo;

public enum AligoMessageType {

    AT("알림톡");

    private final String description;

    AligoMessageType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
