package com.personal.marketnote.community.domain.post;

import com.personal.marketnote.common.utility.FormatConverter;
import lombok.Getter;

@Getter
public enum PostSortProperty {
    ORDER_NUM("지정된 정렬 순서순", "orderNum"),
    ID("기본키(최신순)", "id"),
    IS_ANSWERED("답변 여부", "id");

    private final String description;
    private final String camelCaseValue;
    private final String sortField;

    PostSortProperty(String description, String sortField) {
        this.description = description;
        this.camelCaseValue = FormatConverter.snakeToCamel(this.name());
        this.sortField = sortField;
    }
}
