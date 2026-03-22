package com.personal.marketnote.common.security.hmac.exception;

import org.springframework.security.authentication.BadCredentialsException;

public class HmacNonceReplayedException extends BadCredentialsException {
    public HmacNonceReplayedException(String nonce) {
        super("HMAC 인증 실패: 이미 사용된 Nonce입니다. nonce=" + nonce);
    }
}
