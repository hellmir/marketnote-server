package com.personal.marketnote.community.exception;

public class NotReviewAuthorException extends IllegalStateException {
    private static final String MESSAGE = "ERR_REVIEW_AUTHOR_01::리뷰 작성자가 아닙니다.";

    public NotReviewAuthorException() {
        super(MESSAGE);
    }
}
