package com.personal.marketnote.common.kafka.event;

public enum InventoryChangeAction {
    CREATED,
    UPDATED;

    public boolean isCreated() { return this == CREATED; }
    public boolean isUpdated() { return this == UPDATED; }
}
