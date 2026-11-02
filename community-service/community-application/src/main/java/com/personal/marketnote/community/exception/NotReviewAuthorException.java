package com.personal.marketnote.community.exception;

import lombok.Getter;

@Getter
public class NotReviewAuthorException extends IllegalStateException {
    private static final String NOT_REVIEW_AUTHOR_EXCEPTION_MESSAGE = "리뷰 작성자가 아닙니다. 리뷰 ID: %d, 회원 ID: %d";

    public NotReviewAuthorException(Long id, Long reviewerId) {
        super(String.format(NOT_REVIEW_AUTHOR_EXCEPTION_MESSAGE, id, reviewerId));
    }
}
