package com.personal.marketnote.user.domain.user;

import lombok.Getter;

@Getter
public enum UserPenaltyHistorySortProperty {
    ID("패널티 부과 내역 기본키");

    private final String lowerValue;
    private final String description;

    UserPenaltyHistorySortProperty(String description) {
        lowerValue = this.name().toLowerCase();
        this.description = description;
    }
}
