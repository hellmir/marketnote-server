package com.personal.marketnote.community.exception;

import lombok.Getter;

@Getter
public class InvalidPostContentContainsProfanityException extends IllegalArgumentException {
    private static final String POST_CONTENT_CONTAINS_PROFANITY_EXCEPTION_MESSAGE
            = "게시글에 부적절한 단어가 포함되어 있습니다.";

    public InvalidPostContentContainsProfanityException() {
        super(POST_CONTENT_CONTAINS_PROFANITY_EXCEPTION_MESSAGE);
    }
}
