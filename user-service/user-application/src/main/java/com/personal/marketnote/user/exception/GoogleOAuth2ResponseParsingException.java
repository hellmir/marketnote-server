package com.personal.marketnote.user.exception;

public class GoogleOAuth2ResponseParsingException extends RuntimeException {
    private static final String MESSAGE = "구글 로그인 서비스에서 줘야 할 걸 안 줌:\n%s";

    public GoogleOAuth2ResponseParsingException(String responseBody, Throwable cause) {
        super(String.format(MESSAGE, responseBody), cause);
    }
}
