package com.personal.marketnote.community.exception;

public class InvalidBoardException extends IllegalArgumentException {
    private static final String MESSAGE = "유효하지 않은 게시판입니다.";

    public InvalidBoardException() {
        super(MESSAGE);
    }
}
