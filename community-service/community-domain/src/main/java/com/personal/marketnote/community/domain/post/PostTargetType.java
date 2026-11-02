package com.personal.marketnote.community.domain.post;

import lombok.Getter;

@Getter
public enum PostTargetType {
    PRICE_POLICY("상품 가격 정책");

    private final String description;

    PostTargetType(String description) {
        this.description = description;
    }
}
