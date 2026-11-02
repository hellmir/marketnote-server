package com.personal.marketnote.user.domain.remotearea.exception;

public class InvalidRemoteAreaProvinceLengthException extends IllegalArgumentException {

    public InvalidRemoteAreaProvinceLengthException() {
        super("ERR_REMOTE_AREA_02::도서산간 지역 광역시도는 최대 50자까지 입력할 수 있습니다.");
    }
}
