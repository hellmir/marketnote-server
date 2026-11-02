package com.personal.marketnote.common.domain.exception.illegalargument.novalue;

public class PasswordNoValueException extends NoValueException {
    private static final String PASSWORD_NO_VALUE_EXCEPTION_MESSAGE = "%s:: 비밀번호는 필수값입니다.";

    public PasswordNoValueException(String code) {
        super(String.format(PASSWORD_NO_VALUE_EXCEPTION_MESSAGE, code));
    }
}
