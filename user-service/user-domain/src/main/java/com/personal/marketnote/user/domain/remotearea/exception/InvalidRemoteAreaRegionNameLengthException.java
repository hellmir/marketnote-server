package com.personal.marketnote.user.domain.remotearea.exception;

public class InvalidRemoteAreaRegionNameLengthException extends IllegalArgumentException {

    public InvalidRemoteAreaRegionNameLengthException() {
        super("ERR_REMOTE_AREA_04::도서산간 지역명은 최대 100자까지 입력할 수 있습니다.");
    }
}
