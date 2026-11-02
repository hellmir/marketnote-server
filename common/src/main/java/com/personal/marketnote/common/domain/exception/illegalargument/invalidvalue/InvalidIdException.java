package com.personal.marketnote.common.domain.exception.illegalargument.invalidvalue;

public class InvalidIdException extends InvalidValueException {
    private static final String INVALID_ID_EXCEPTION_MESSAGE
            = "ID는 양의 정수(1 이상의 숫자값)여야 합니다. 전송된 ID: %s";

    public InvalidIdException(String value) {
        super(String.format(INVALID_ID_EXCEPTION_MESSAGE, value));
    }
}
