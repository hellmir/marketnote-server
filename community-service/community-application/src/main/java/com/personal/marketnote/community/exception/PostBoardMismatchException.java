package com.personal.marketnote.community.exception;

public class PostBoardMismatchException extends IllegalArgumentException {
    private static final String POST_BOARD_MISMATCH_EXCEPTION_MESSAGE
            = "요청한 게시판과 실제 게시판이 일치하지 않습니다.";

    public PostBoardMismatchException() {
        super(POST_BOARD_MISMATCH_EXCEPTION_MESSAGE);
    }
}
