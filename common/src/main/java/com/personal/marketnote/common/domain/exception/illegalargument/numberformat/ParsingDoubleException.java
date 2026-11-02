package com.personal.marketnote.common.domain.exception.illegalargument.numberformat;

public class ParsingDoubleException extends NumberFormatException {
    private static final String PARSING_DOUBLE_EXCEPTION_MESSAGE
            = "소수값만 Double 타입으로 변환할 수 있습니다. 현재 변환 대상 값: %s";

    public ParsingDoubleException(String value) {
        super(String.format(PARSING_DOUBLE_EXCEPTION_MESSAGE, value));
    }
}
