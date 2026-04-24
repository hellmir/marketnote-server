package com.personal.marketnote.user.domain.remotearea.exception;

public class RemoteAreaProvinceNoValueException extends RuntimeException {

    public RemoteAreaProvinceNoValueException() {
        super("ERR_REMOTE_AREA_01::도서산간 지역 광역시도는 필수입니다.");
    }
}
