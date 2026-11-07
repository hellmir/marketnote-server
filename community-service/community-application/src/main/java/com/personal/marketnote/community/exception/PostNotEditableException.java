package com.personal.marketnote.community.exception;

public class PostNotEditableException extends IllegalStateException {
    private static final String POST_NOT_EDITABLE_EXCEPTION_MESSAGE
            = "ERR_POST_EDIT_01::수정할 수 없는 게시판입니다.";

    public PostNotEditableException() {
        super(POST_NOT_EDITABLE_EXCEPTION_MESSAGE);
    }
}
