package com.personal.marketnote.community.exception;

public class InvalidBoardException extends IllegalArgumentException {
    private static final String INVALID_BOARD_EXCEPTION_MESSAGE
            = "유효하지 않은 게시판입니다. 전송된 게시판: %s";

    public InvalidBoardException(String board) {
        super(String.format(INVALID_BOARD_EXCEPTION_MESSAGE, board));
    }
}
