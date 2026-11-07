package com.personal.marketnote.community.domain.post.exception;

public class InvalidPostCategoryException extends IllegalArgumentException {
    private static final String MESSAGE = "유효하지 않은 카테고리명입니다.";

    public InvalidPostCategoryException() {
        super(MESSAGE);
    }
}
