package com.personal.marketnote.user.exception;

import com.personal.marketnote.common.domain.exception.DomainNotFoundException;

public class RemoteAreaNotFoundException extends DomainNotFoundException {
    private static final String MESSAGE = "ERR_REMOTE_AREA_07::도서산간 지역을 찾을 수 없습니다.";

    public RemoteAreaNotFoundException() {
        super(MESSAGE);
    }
}
