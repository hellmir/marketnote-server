package com.personal.marketnote.user.domain.remotearea.exception;

public class InvalidRemoteAreaDistrictLengthException extends IllegalArgumentException {

    public InvalidRemoteAreaDistrictLengthException() {
        super("ERR_REMOTE_AREA_03::도서산간 지역 읍면동은 최대 50자까지 입력할 수 있습니다.");
    }
}
