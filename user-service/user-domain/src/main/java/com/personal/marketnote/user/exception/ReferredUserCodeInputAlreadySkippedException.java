package com.personal.marketnote.user.exception;

import com.personal.marketnote.common.domain.exception.DomainAlreadyExistsException;

public class ReferredUserCodeInputAlreadySkippedException extends DomainAlreadyExistsException {
    private static final String MESSAGE = "ERR03::이미 추천 회원 초대 코드 입력을 건너뛰었습니다.";

    public ReferredUserCodeInputAlreadySkippedException() {
        super(MESSAGE);
    }
}
