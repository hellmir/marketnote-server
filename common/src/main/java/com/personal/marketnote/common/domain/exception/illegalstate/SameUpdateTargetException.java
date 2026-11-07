package com.personal.marketnote.common.domain.exception.illegalstate;

public class SameUpdateTargetException extends IllegalStateException {
    private static final String MESSAGE = "ERR_SAME_UPDATE_TARGET_01::업데이트할 대상의 값과 입력한 값이 동일합니다.";

    public SameUpdateTargetException() {
        super(MESSAGE);
    }
}
