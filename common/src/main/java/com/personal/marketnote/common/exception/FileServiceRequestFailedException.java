package com.personal.marketnote.common.exception;

import java.io.IOException;

public class FileServiceRequestFailedException extends ExternalOperationFailedException {
    private static final String FILE_SERVICE_REQUEST_FAILED_EXCEPTION_MESSAGE = "파일 서비스 통신 중 오류가 발생했습니다.";

    public FileServiceRequestFailedException(IOException cause) {
        super(String.format(FILE_SERVICE_REQUEST_FAILED_EXCEPTION_MESSAGE), cause);
    }
}
