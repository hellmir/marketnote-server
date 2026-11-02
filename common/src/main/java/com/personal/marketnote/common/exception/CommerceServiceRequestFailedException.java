package com.personal.marketnote.common.exception;

import java.io.IOException;

public class CommerceServiceRequestFailedException extends ExternalOperationFailedException {
    private static final String COMMERCE_SERVICE_REQUEST_FAILED_EXCEPTION_MESSAGE = "커머스 서비스 통신 중 오류가 발생했습니다.";

    public CommerceServiceRequestFailedException(IOException cause) {
        super(String.format(COMMERCE_SERVICE_REQUEST_FAILED_EXCEPTION_MESSAGE), cause);
    }
}
