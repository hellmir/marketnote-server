package com.personal.marketnote.user.utility.jwt.exception;

import com.personal.marketnote.user.utility.jwt.JwtTokenType;

public class InvalidJwtTokenTypeException extends IllegalArgumentException {
    private static final String MESSAGE = "Expected parsing %s, but just attempted to parse %s";

    public InvalidJwtTokenTypeException(JwtTokenType expected, JwtTokenType actual) {
        super(String.format(MESSAGE, expected, actual));
    }
}
