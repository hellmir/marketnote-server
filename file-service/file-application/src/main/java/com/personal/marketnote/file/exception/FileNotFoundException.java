package com.personal.marketnote.file.exception;

import com.personal.marketnote.common.domain.exception.DomainNotFoundException;

public class FileNotFoundException extends DomainNotFoundException {
    private static final String MESSAGE = "파일을 찾을 수 없습니다.";

    public FileNotFoundException() {
        super(MESSAGE);
    }
}
