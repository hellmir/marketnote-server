package com.personal.marketnote.file.domain.file.exception;

import com.personal.marketnote.common.domain.exception.BusinessSecurityException;

public class InvalidFileOwnerException extends BusinessSecurityException {
    private static final String MESSAGE = "ERR_FILE_OWNER_01::파일 소유자 정보가 일치하지 않습니다.";

    public InvalidFileOwnerException() {
        super(MESSAGE);
    }
}
