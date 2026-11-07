package com.personal.marketnote.user.exception;

import com.personal.marketnote.common.domain.exception.DomainAlreadyExistsException;

public class RemoteAreaAlreadyExistsException extends DomainAlreadyExistsException {
    private static final String MESSAGE = "ERR_REMOTE_AREA_06::이미 등록된 도서산간 지역입니다.";

    public RemoteAreaAlreadyExistsException() {
        super(MESSAGE);
    }
}
