package com.personal.marketnote.community.domain.post.exception;

public class InvalidPostCategoryForBoardException extends IllegalArgumentException {
    private static final String MESSAGE = "게시판에 맞지 않는 카테고리입니다.";

    public InvalidPostCategoryForBoardException() {
        super(MESSAGE);
    }
}
