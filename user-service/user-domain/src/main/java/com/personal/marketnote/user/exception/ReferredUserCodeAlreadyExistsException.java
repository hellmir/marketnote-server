package com.personal.marketnote.user.exception;

import lombok.Getter;

@Getter
public class ReferredUserCodeAlreadyExistsException extends IllegalStateException {
    private static final String REFERRED_USER_CODE_ALREADY_EXISTS_EXCEPTION_MESSAGE
            = "%s:: 이미 추천 회원 초대 코드를 등록했습니다.";

    public ReferredUserCodeAlreadyExistsException(String code) {
        super(String.format(REFERRED_USER_CODE_ALREADY_EXISTS_EXCEPTION_MESSAGE, code));
    }
}
