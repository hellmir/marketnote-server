package com.personal.marketnote.common.domain.exception.illegalargument.novalue;

public class EmailNoValueException extends NoValueException {
    private static final String EMAIL_NO_VALUE_EXCEPTION_MESSAGE = "이메일 주소는 필수값입니다.";

    public EmailNoValueException() {
        super(EMAIL_NO_VALUE_EXCEPTION_MESSAGE);
    }
}
