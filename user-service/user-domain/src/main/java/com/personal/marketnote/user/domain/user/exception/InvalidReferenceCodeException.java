package com.personal.marketnote.user.domain.user.exception;

public class InvalidReferenceCodeException extends IllegalArgumentException {
    private static final String MESSAGE
            = "ERR_USER_REFERENCE_CODE_FORMAT_01::추천인 코드 형식이 올바르지 않습니다.";

    public InvalidReferenceCodeException() {
        super(MESSAGE);
    }
}
