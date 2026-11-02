package com.personal.marketnote.user.exception;

import lombok.Getter;
import org.springframework.security.authentication.BadCredentialsException;

@Getter
public class InvalidVerificationCodeException extends BadCredentialsException {
    private static final String INVALID_EMAIL_VERIFICATION_CODE_EXCEPTION_MESSAGE
            = "%s:: 이메일 인증 코드가 유효하지 않거나 만료되었습니다. 전송된 이메일 주소: %s";

    public InvalidVerificationCodeException(String code, String email) {
        super(String.format(INVALID_EMAIL_VERIFICATION_CODE_EXCEPTION_MESSAGE, code, email));
    }
}
