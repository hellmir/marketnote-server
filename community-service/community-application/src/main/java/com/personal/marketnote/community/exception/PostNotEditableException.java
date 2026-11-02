package com.personal.marketnote.community.exception;

import lombok.Getter;

@Getter
public class PostNotEditableException extends IllegalStateException {
    private static final String POST_NOT_EDITABLE_EXCEPTION_MESSAGE
            = "수정할 수 없는 게시판입니다.";

    public PostNotEditableException() {
        super(String.format(POST_NOT_EDITABLE_EXCEPTION_MESSAGE));
    }
}
