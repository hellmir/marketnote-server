package com.personal.marketnote.file.domain.file.exception;

import com.personal.marketnote.common.domain.exception.BusinessSecurityException;

public class InvalidFileRoleException extends BusinessSecurityException {
    private static final String MESSAGE = "ERR_FILE_ROLE_01::해당 파일 종류를 업로드할 권한이 없습니다.";

    public InvalidFileRoleException() {
        super(MESSAGE);
    }
}
