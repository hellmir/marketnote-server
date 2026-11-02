package com.personal.marketnote.common.domain.exception.illegalargument.numberformat;

public class ParsingLongException extends NumberFormatException {
    private static final String PARSING_LONG_EXCEPTION_MESSAGE
            = "숫자값만 Long 타입으로 변환할 수 있습니다. 현재 변환 대상 값: %s";

    public ParsingLongException(String value) {
        super(String.format(PARSING_LONG_EXCEPTION_MESSAGE, value));
    }
}
