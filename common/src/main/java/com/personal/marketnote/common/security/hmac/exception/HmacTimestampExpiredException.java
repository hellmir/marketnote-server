package com.personal.marketnote.common.security.hmac.exception;

import org.springframework.security.authentication.BadCredentialsException;

public class HmacTimestampExpiredException extends BadCredentialsException {
    public HmacTimestampExpiredException(String timestamp) {
        super("HMAC 인증 실패: Timestamp가 허용 범위를 초과했습니다. timestamp=" + timestamp);
    }
}
