package com.personal.marketnote.user.exception;

import com.personal.marketnote.common.domain.exception.DomainAlreadyExistsException;

public class ReferredUserCodeAlreadyExistsException extends DomainAlreadyExistsException {
    private static final String MESSAGE = "ERR02::이미 추천 회원 초대 코드를 등록했습니다.";

    public ReferredUserCodeAlreadyExistsException() {
        super(MESSAGE);
    }
}
