package com.personal.marketnote.user.domain.remotearea.exception;

public class InvalidRemoteAreaZipCodeException extends IllegalArgumentException {

    public InvalidRemoteAreaZipCodeException() {
        super("ERR_REMOTE_AREA_01::유효하지 않은 우편번호입니다. 우편번호는 5자리 숫자여야 합니다.");
    }
}
