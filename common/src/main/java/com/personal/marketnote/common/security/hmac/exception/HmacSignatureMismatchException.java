package com.personal.marketnote.common.security.hmac.exception;

import org.springframework.security.authentication.BadCredentialsException;

public class HmacSignatureMismatchException extends BadCredentialsException {
    public HmacSignatureMismatchException() {
        super("HMAC 인증 실패: 서명이 일치하지 않습니다.");
    }
}
