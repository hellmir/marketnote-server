package com.personal.marketnote.community.domain.post.exception;

public class InvalidPostCategoryForBoardException extends IllegalArgumentException {
    private static final String INVALID_POST_CATEGORY_FOR_BOARD_EXCEPTION_MESSAGE
            = "게시판에 맞지 않는 카테고리입니다. 카테고리명: %s";

    public InvalidPostCategoryForBoardException(String categoryCode) {
        super(String.format(INVALID_POST_CATEGORY_FOR_BOARD_EXCEPTION_MESSAGE, categoryCode));
    }
}
