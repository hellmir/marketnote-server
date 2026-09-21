package com.personal.marketnote.user.domain.user;

public enum UserStatusAction {
    DEACTIVATE("비활성화"),
    ACTIVATE("활성화");

    private final String description;

    UserStatusAction(String description) {
        this.description = description;
    }

    public boolean isDeactivate() {
        return this == DEACTIVATE;
    }

    public boolean isActivate() {
        return this == ACTIVATE;
    }

    public String getDescription() {
        return description;
    }
}
