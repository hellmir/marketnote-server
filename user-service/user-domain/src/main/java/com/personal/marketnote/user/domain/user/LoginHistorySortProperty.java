package com.personal.marketnote.user.domain.user;

import lombok.Getter;

@Getter
public enum LoginHistorySortProperty {
    ID("로그인 내역 기본키"),
    USER_ID("회원 기본키");

    private final String lowerValue;
    private final String description;

    LoginHistorySortProperty(String description) {
        lowerValue = this.name().toLowerCase();
        this.description = description;
    }
}
