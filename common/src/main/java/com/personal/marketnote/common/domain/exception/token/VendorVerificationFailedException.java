package com.personal.marketnote.common.domain.exception.token;

import org.springframework.security.authentication.BadCredentialsException;

public class VendorVerificationFailedException extends BadCredentialsException {
    public VendorVerificationFailedException(String message) {
        super(message);
    }
}
