package com.personal.marketnote.user.domain.remotearea.exception;

public class InvalidRemoteAreaSubareaLengthException extends IllegalArgumentException {

    public InvalidRemoteAreaSubareaLengthException() {
        super("ERR_REMOTE_AREA_05::도서산간 지역 세부지역은 최대 50자까지 입력할 수 있습니다.");
    }
}
