package com.personal.marketnote.community.domain.post;

import lombok.Getter;

@Getter
public enum PostTargetGroupType {
    PRODUCT("상품");

    private final String description;

    PostTargetGroupType(String description) {
        this.description = description;
    }
}
