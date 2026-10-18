package com.personal.marketnote.common.security.hmac.exception;

import org.springframework.security.authentication.AuthenticationServiceException;

public class HmacNonceValidationUnavailableException extends AuthenticationServiceException {
    public HmacNonceValidationUnavailableException(String nonce, Throwable cause) {
        super("HMAC Nonce 검증 불가: Redis 장애로 재전송 방지를 수행할 수 없습니다. nonce=" + nonce, cause);
    }
}
