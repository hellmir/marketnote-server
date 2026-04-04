package com.personal.marketnote.community.exception;

import lombok.Getter;

@Getter
public class InvalidReviewContentContainsProfanityException extends IllegalArgumentException {
    private static final String REVIEW_CONTENT_CONTAINS_PROFANITY_EXCEPTION_MESSAGE
            = "리뷰에 부적절한 단어가 포함되어 있습니다.";

    public InvalidReviewContentContainsProfanityException() {
        super(REVIEW_CONTENT_CONTAINS_PROFANITY_EXCEPTION_MESSAGE);
    }
}
