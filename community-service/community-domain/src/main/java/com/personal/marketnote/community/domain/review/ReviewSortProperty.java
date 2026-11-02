package com.personal.marketnote.community.domain.review;

import lombok.Getter;

@Getter
public enum ReviewSortProperty {
    ID("기본키(최신순)", "id"),
    LIKE("도움돼요순", "likeCount"),
    RATING("평점순", "rating"),
    ORDER_NUM("정렬 순서", "orderNum");

    private final String description;
    private final String camelCaseValue;

    ReviewSortProperty(String description, String camelCaseValue) {
        this.description = description;
        this.camelCaseValue = camelCaseValue;
    }
}
