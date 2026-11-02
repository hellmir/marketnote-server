package com.personal.marketnote.common.domain.exception.illegalstate;

import lombok.Getter;

@Getter
public class SameUpdateTargetException extends IllegalStateException {
    private static final String SAME_UPDATE_TARGET_EXCEPTION_MESSAGE = "%s:: 업데이트할 대상의 값과 입력한 값이 동일합니다. 전송한 값: %s";

    public SameUpdateTargetException(String code, String target) {
        super(String.format(SAME_UPDATE_TARGET_EXCEPTION_MESSAGE, code, target));
    }
}
