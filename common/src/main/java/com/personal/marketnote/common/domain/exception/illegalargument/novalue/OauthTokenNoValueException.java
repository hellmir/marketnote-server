package com.personal.marketnote.common.domain.exception.illegalargument.novalue;

public class OauthTokenNoValueException extends NoValueException {
    private static final String OAUTH_TOKEN_NO_VALUE_EXCEPTION_MESSAGE
            = "OAuth2 토큰이 존재하지 않습니다. 로그인 후 다시 시도해 주세요.";

    public OauthTokenNoValueException() {
        super(OAUTH_TOKEN_NO_VALUE_EXCEPTION_MESSAGE);
    }
}
