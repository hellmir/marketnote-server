package com.personal.marketnote.user.utility.jwt.exception;

public class InvalidOAuth2VendorException extends IllegalArgumentException {
    private static final String MESSAGE = "존재하지 않는 OAuth2 공급 업체입니다. issuer: %s";

    public InvalidOAuth2VendorException(String issuer) {
        super(String.format(MESSAGE, issuer));
    }
}
