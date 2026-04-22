package com.personal.marketnote.user.domain.remotearea.exception;

public class RemoteAreaRegionNameNoValueException extends RuntimeException {

    public RemoteAreaRegionNameNoValueException() {
        super("ERR_REMOTE_AREA_03::도서산간 지역명은 필수입니다.");
    }
}
