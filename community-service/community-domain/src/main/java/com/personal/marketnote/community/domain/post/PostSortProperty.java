package com.personal.marketnote.community.domain.post;

import com.personal.marketnote.common.utility.FormatConverter;
import lombok.Getter;

@Getter
public enum PostSortProperty {
    ORDER_NUM("지정된 정렬 순서순"),
    ID("기본키(최신순)"),
    IS_ANSWERED("답변 여부");

    private final String description;
    private final String camelCaseValue;

    PostSortProperty(String description) {
        this.description = description;
        this.camelCaseValue = FormatConverter.snakeToCamel(this.name());
    }
}
