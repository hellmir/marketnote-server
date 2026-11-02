package com.personal.marketnote.community.domain.like;

import com.personal.marketnote.common.utility.FormatConverter;
import lombok.Getter;

@Getter
public enum LikeTargetType {
    REVIEW("리뷰"),
    BOARD("게시글");

    private final String description;
    private final String camelCaseValue;

    LikeTargetType(String description) {
        this.description = description;
        this.camelCaseValue = FormatConverter.snakeToCamel(name());
    }
}
