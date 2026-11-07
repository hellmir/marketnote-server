package com.personal.marketnote.user.exception;

public class InvalidNicknameContainsProfanityException extends IllegalArgumentException {
    private static final String MESSAGE = "ERR_USER_NICKNAME_PROFANITY_01::닉네임에 부적절한 단어가 포함되어 있습니다.";

    public InvalidNicknameContainsProfanityException() {
        super(MESSAGE);
    }
}
