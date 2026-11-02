package com.personal.marketnote.user.exception;

import lombok.Getter;

@Getter
public class InvalidNicknameContainsProfanityException extends IllegalArgumentException {
    private static final String NICKNAME_CONTAINS_PROFANITY_EXCEPTION_MESSAGE
            = "%s:: 닉네임에 부적절한 단어가 포함되어 있습니다. 전송된 닉네임: %s";

    public InvalidNicknameContainsProfanityException(String code, String nickname) {
        super(String.format(NICKNAME_CONTAINS_PROFANITY_EXCEPTION_MESSAGE, code, nickname));
    }
}
