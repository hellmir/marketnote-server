package com.personal.marketnote.common.domain.exception.illegalargument.invalidvalue;

public class ParsingBooleanException extends InvalidValueException {
    private static final String PARSING_BOOLEAN_EXCEPTION_MESSAGE
            = "논리형 값만 Boolean 타입으로 변환할 수 있습니다. 현재 변환 대상 값: %s";

    public ParsingBooleanException(String value) {
        super(String.format(PARSING_BOOLEAN_EXCEPTION_MESSAGE, value));
    }
}
