package com.personal.marketnote.user.exception;

import org.springframework.security.authentication.BadCredentialsException;

public class InvalidVerificationCodeException extends BadCredentialsException {
    private static final String MESSAGE = "ERR_USER_VERIFICATION_CODE_01::이메일 인증 코드가 유효하지 않거나 만료되었습니다.";

    public InvalidVerificationCodeException() {
        super(MESSAGE);
    }
}
