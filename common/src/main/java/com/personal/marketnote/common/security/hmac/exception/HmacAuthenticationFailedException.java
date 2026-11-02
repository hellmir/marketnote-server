package com.personal.marketnote.common.security.hmac.exception;

import org.springframework.security.authentication.BadCredentialsException;

public class HmacAuthenticationFailedException extends BadCredentialsException {
    public HmacAuthenticationFailedException(String message) {
        super(message);
    }
}
