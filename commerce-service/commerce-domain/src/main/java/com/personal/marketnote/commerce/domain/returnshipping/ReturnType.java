package com.personal.marketnote.commerce.domain.returnshipping;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ReturnType {
    FULL_RETURN("전체 반품"),
    PARTIAL_RETURN("부분 반품");

    private final String description;

    public boolean isFullReturn() {
        return this == FULL_RETURN;
    }

    public boolean isPartialReturn() {
        return this == PARTIAL_RETURN;
    }
}
