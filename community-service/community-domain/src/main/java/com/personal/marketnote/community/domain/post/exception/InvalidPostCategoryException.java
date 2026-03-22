package com.personal.marketnote.community.domain.post.exception;

public class InvalidPostCategoryException extends IllegalArgumentException {
    private static final String INVALID_POST_CATEGORY_EXCEPTION_MESSAGE
            = "유효하지 않은 카테고리명입니다. 카테고리명: %s";

    public InvalidPostCategoryException(String categoryCode) {
        super(String.format(INVALID_POST_CATEGORY_EXCEPTION_MESSAGE, categoryCode));
    }
}
