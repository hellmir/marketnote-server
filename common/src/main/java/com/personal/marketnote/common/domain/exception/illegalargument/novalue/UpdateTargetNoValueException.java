package com.personal.marketnote.common.domain.exception.illegalargument.novalue;

public class UpdateTargetNoValueException extends NoValueException {
    private static final String UPDATE_TARGET_NO_VALUE_EXCEPTION_MESSAGE = "업데이트할 대상과 값을 전송해야 합니다.";

    public UpdateTargetNoValueException() {
        super(UPDATE_TARGET_NO_VALUE_EXCEPTION_MESSAGE);
    }
}
