package com.personal.marketnote.common.domain.exception.accessdenied;

import org.springframework.security.authentication.BadCredentialsException;

public class LoginFailedException extends BadCredentialsException {
    public LoginFailedException(String message) {
        super(message);
    }
}
