package com.personal.marketnote.common.domain.exception;

public class ExceptionMessage {
    public static final String INVALID_EMAIL_EXCEPTION_MESSAGE
            = "이메일 주소 형식이 잘못되었습니다. 예: abcd@abc.com\n전송된 이메일 주소: %s";
    public static final String INVALID_ID_EXCEPTION_MESSAGE = "ID는 양의 정수(1 이상의 숫자값)여야 합니다. 전송된 ID: %s";
    public static final String INVALID_ACCESS_TOKEN_EXCEPTION_MESSAGE = "유효하지 않은 Access Token입니다.";
}
