package com.personal.marketnote.user.domain.user;

public enum UserStatusActor {
    ADMIN, SYSTEM;

    public boolean isAdmin() {
        return this == ADMIN;
    }

    public boolean isSystem() {
        return this == SYSTEM;
    }
}
