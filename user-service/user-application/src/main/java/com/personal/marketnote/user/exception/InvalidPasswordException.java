package com.personal.marketnote.user.exception;

import lombok.Getter;
import org.springframework.security.authentication.BadCredentialsException;

@Getter
public class InvalidPasswordException extends BadCredentialsException {
    private static final String INVALID_PASSWORD_EXCEPTION_MESSAGE = "%s:: 회원 비밀번호가 일치하지 않습니다.";

    public InvalidPasswordException(String code) {
        super(String.format(INVALID_PASSWORD_EXCEPTION_MESSAGE, code));
    }
}
