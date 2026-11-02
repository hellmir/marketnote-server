package com.personal.marketnote.common.exception;

import lombok.Getter;

@Getter
public class NotOAuth2UserException extends IllegalArgumentException {
    private static final String NOT_OAUTH2_USER_EXCEPTION_MESSAGE = "OAuth2 회원이 아닙니다.";

    public NotOAuth2UserException() {
        super(NOT_OAUTH2_USER_EXCEPTION_MESSAGE);
    }
}
