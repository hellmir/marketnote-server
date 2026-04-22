package com.personal.marketnote.user.domain.remotearea.exception;

public class RemoteAreaTypeNoValueException extends RuntimeException {

    public RemoteAreaTypeNoValueException() {
        super("ERR_REMOTE_AREA_02::도서산간 지역 유형은 필수입니다.");
    }
}
