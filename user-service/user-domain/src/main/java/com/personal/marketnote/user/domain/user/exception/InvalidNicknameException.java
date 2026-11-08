package com.personal.marketnote.user.domain.user.exception;

public class InvalidNicknameException extends IllegalArgumentException {
    private static final String MESSAGE
            = "ERR_USER_NICKNAME_FORMAT_01::닉네임 형식이 올바르지 않습니다.";

    public InvalidNicknameException() {
        super(MESSAGE);
    }
}
