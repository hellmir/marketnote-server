package com.personal.marketnote.user.domain.remotearea;

public enum RemoteAreaType {
    JEJU("제주"),
    ISLAND_MOUNTAINOUS("도서산간");

    private final String description;

    RemoteAreaType(String description) {
        this.description = description;
    }

    public boolean isJeju() {
        return this == JEJU;
    }

    public boolean isIslandMountainous() {
        return this == ISLAND_MOUNTAINOUS;
    }
}
