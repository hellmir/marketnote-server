package com.personal.marketnote.file.domain.file.exception;

import com.personal.marketnote.common.domain.exception.BusinessSecurityException;

public class InvalidSvgFileException extends BusinessSecurityException {
    private static final String MESSAGE = "ERR_FILE_SVG_01::유효하지 않거나 안전하지 않은 SVG 파일입니다.";

    public InvalidSvgFileException() {
        super(MESSAGE);
    }
}
