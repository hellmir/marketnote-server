package com.personal.marketnote.user.exception;

public class OAuth2ResponseParsingException extends RuntimeException {
    private static final String MESSAGE = "OAuth2 벤더(%s) 응답 파싱 실패:\n%s";

    public OAuth2ResponseParsingException(String vendor, String responseBody, Throwable cause) {
        super(String.format(MESSAGE, vendor, responseBody), cause);
    }
}
