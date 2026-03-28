package com.personal.marketnote.common.exception;

import java.io.IOException;

public class UserServiceRequestFailedException extends ExternalOperationFailedException {
    private static final String USER_SERVICE_REQUEST_FAILED_EXCEPTION_MESSAGE = "회원 서비스 통신 중 오류가 발생했습니다.";

    public UserServiceRequestFailedException(IOException cause) {
        super(String.format(USER_SERVICE_REQUEST_FAILED_EXCEPTION_MESSAGE), cause);
    }
}
