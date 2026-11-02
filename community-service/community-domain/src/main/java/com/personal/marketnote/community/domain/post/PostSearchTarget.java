package com.personal.marketnote.community.domain.post;

import com.personal.marketnote.common.utility.FormatConverter;
import lombok.Getter;

@Getter
public enum PostSearchTarget {
    TITLE("게시글 제목"),
    CONTENT("게시글 내용");

    private final String description;
    private final String camelCaseValue;

    PostSearchTarget(String description) {
        this.description = description;
        this.camelCaseValue = FormatConverter.snakeToCamel(this.name());
    }
}
