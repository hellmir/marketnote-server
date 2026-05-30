package com.personal.marketnote.community.domain.post;

import com.personal.marketnote.common.utility.FormatConverter;
import lombok.Getter;

@Getter
public enum PostFilterCategory {
    IS_PUBLIC("비밀글 여부"),
    IS_MINE("내 문의글 여부");

    private final String description;
    private final String camelCaseValue;

    PostFilterCategory(String description) {
        this.description = description;
        camelCaseValue = FormatConverter.snakeToCamel(name());
    }

    public boolean isPublicOnly(PostFilterValue filterValue) {
        return isPublic() && filterValue.isTrue();
    }

    private boolean isPublic() {
        return this == PostFilterCategory.IS_PUBLIC;
    }

    public boolean isMineFiltered(PostFilterValue filterValue) {
        return isMine() && filterValue.isTrue();
    }

    private boolean isMine() {
        return this == PostFilterCategory.IS_MINE;
    }
}
