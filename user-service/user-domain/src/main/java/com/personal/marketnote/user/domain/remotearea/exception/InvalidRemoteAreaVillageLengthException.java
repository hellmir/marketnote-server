package com.personal.marketnote.user.domain.remotearea.exception;

public class InvalidRemoteAreaVillageLengthException extends IllegalArgumentException {

    public InvalidRemoteAreaVillageLengthException() {
        super("ERR_REMOTE_AREA_04::도서산간 지역 리는 최대 50자까지 입력할 수 있습니다.");
    }
}
