package com.personal.marketnote.community.domain.post.exception;

public class BoardOrCategoryNoValueException extends IllegalArgumentException {
    private static final String BOARD_OR_CATEGORY_NO_VALUE_EXCEPTION_MESSAGE
            = "게시판 또는 카테고리가 없습니다.";

    public BoardOrCategoryNoValueException() {
        super(BOARD_OR_CATEGORY_NO_VALUE_EXCEPTION_MESSAGE);
    }
}
