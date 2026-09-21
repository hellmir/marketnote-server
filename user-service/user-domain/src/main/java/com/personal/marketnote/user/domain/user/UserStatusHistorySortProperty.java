package com.personal.marketnote.user.domain.user;

import lombok.Getter;

@Getter
public enum UserStatusHistorySortProperty {
    ID("상태 변경 내역 기본키");

    private final String lowerValue;
    private final String description;

    UserStatusHistorySortProperty(String description) {
        lowerValue = this.name().toLowerCase();
        this.description = description;
    }
}
