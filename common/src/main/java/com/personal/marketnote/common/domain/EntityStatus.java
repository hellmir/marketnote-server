package com.personal.marketnote.common.domain;

public enum EntityStatus {
    ACTIVE("활성화"),
    INACTIVE("비활성화"),
    UNEXPOSED("비노출");

    private final String description;

    EntityStatus(String description) {
        this.description = description;
    }

    public boolean isActive() {
        return this == ACTIVE;
    }

    public boolean isInactive() {
        return this == INACTIVE;
    }

    public static EntityStatus from(boolean isActive) {
        if (isActive) {
            return ACTIVE;
        }

        return INACTIVE;
    }

    public static EntityStatus changeVisibility(EntityStatus status) {
        if (status.isActive()) {
            return UNEXPOSED;
        }

        return ACTIVE;
    }
}
