package com.personal.marketnote.common.kafka.event;

public enum ShippingPolicyChangeAction {
    CREATED,
    UPDATED,
    DELETED;

    public boolean isCreated() {
        return this == CREATED;
    }

    public boolean isUpdated() {
        return this == UPDATED;
    }

    public boolean isDeleted() {
        return this == DELETED;
    }
}
